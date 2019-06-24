package crawlnet.core.component

import akka.actor.{Actor, ActorRef, ActorSystem, Address}
import crawlnet.core.logs.CLog
import scala.util.Random


/** Basic Component for distribute system
  * */
trait ComponentUnit {
  val componentId:String
  val componentName:String
  val componentType:String
  var componentStatus:String =_
}

private[component] trait AkkaUnit extends ComponentUnit with Actor with CLog{

  val param:AkkaParameter = new AkkaParameter
  val componentHost:AkkaHost = AkkaHost(context.system.settings.config.getString("akka.remote.netty.tcp.hostname"),
                                        context.system.settings.config.getInt("akka.remote.netty.tcp.port"))
  val akkaAddressBook = new AkkaAddressBook
  param.setHostAndPort(componentHost.hostname, componentHost.port)
  param.setAkkaAddress(componentHost.toAkkaAddress(self)(context.system))

  var receiveSet:Map[String, Receive] = Map.empty[String, Receive]

  def initial(): Unit = {
    param.setComponentName(componentName)
    param.setComponentType(componentType)
    param.setComponentId(componentId)
    akkaAddressBook.addAddress(componentId, componentName, componentType, param.getAkkaAddress)
    info(s"[$componentId, $componentName, $componentType]: server is running, address:${param.getAkkaAddress}")
    info(s"[$componentId, $componentName, $componentType]: register receiver:" + getReceiverNames.mkString(","))
  }

  final def receiveAdd(tag:String, other:Receive):Unit = {
    receiveSet += (tag -> other)
    context.become(receive)
    param.setReceiverNames(getReceiverNames)
  }

  final def receiveRemove(tag:String):Unit = {
    receiveSet -= tag
    context.become(receive)
  }

  final def getReceiverNames:Iterable[String] = receiveSet.keys

  final def receive: Receive = receiveSet.values.reduce(_ orElse _)

  final def sendMessageById(componentId:String, msg:Any): Unit =
    akkaAddressBook
      .getAddressById(componentId)
      .map(context.actorSelection).foreach(_ ! msg)

  final def sendMessageByName(componentId:String, msg:Any):Unit = {
    akkaAddressBook
      .getAddressByName(componentId)
      .map(context.actorSelection).foreach(_ ! msg)
  }

  final def sendMessageByType(componentType:String, msg:Any):Unit = {
    val addresses = akkaAddressBook.getAddressByType(componentType).toArray
    if(!addresses.isEmpty)
      if(addresses.length == 1)
        context.actorSelection(addresses.head) ! msg
      else
        context.actorSelection(addresses(Random.nextInt() % addresses.length)) ! msg
  }
}


/** Host for Component
  * */

case class AkkaHost(hostname:String, port:Int) extends HostAkkaConvert{
  override def toString:String = s"$hostname:$port"
}

trait HostAkkaConvert{
  self:AkkaHost=>
  def toAkkaAddress(actor: ActorRef)(implicit as:ActorSystem): String = {
    "akka." + actor.path.toStringWithAddress(Address("tcp", as.name, hostname, port))
  }
}

/** Parameters in an component.
  * */
class AkkaParameter
  extends Parameter
  with AkkaParameterOp
  with UnitParameterOp


trait Parameter{

  private var param:Map[String, Any] = Map.empty[String, Any]

  def addProperty(key:String, value:Any): Unit ={
    param += (key -> value)
  }

  def addProperties[T](iter:Traversable[(String, T)]): Unit ={
    iter.foreach{
      case (key, value) =>
        addProperty(key, value)
    }
  }

  def deleteProperty(key:String):Unit = {
    param -= key
  }

  def get[T](key:String):Option[T] = {
    param.get(key).map(_.asInstanceOf[T])
  }

  def getString(key:String):Option[String] = {
    get[String](key)
  }

  def getInt(key:String):Option[Int] = {
    get[Int](key)
  }

  def getDouble(key:String):Option[Double] = {
    get[Double](key)
  }

  def getBoolean(key:String):Option[Boolean] = {
    get[Boolean](key)
  }

  def getParameters: Map[String, Any] = param

  def merge(other:Parameter): Unit ={
    other.param.foreach{
      case(key, value) =>
        this.param += (key -> value)
    }
  }

  def mergeWithMap(p:Map[String, String]): Unit = {
    p.foreach{
      case(key, value) =>
        this.param += (key -> value)
    }
  }
}

trait UnitParameterOp{
  self:Parameter =>

  def setComponentId(id:String):Unit = {
    addProperty("Component_ID", id)
  }

  def setComponentName(name:String):Unit = {
    addProperty("Component_Name", name)
  }

  def setComponentType(componentType:String):Unit = {
    addProperty("Component_Type", componentType)
  }

  def getComponentId:String = {
    get[String]("Component_ID").get
  }

  def getComponentName:String = {
    get[String]("Component_Name").get
  }

  def getComponentType:String = {
    get[String]("Component_Type").get
  }
}
trait AkkaParameterOp{
  self:Parameter =>

  def setAkkaAddress(address:String): Unit ={
    addProperty("Akka_Address", address)
  }

  def getAkkaAddress: String = {
    get[String]("Akka_Address").get
  }

  def setHostAndPort(host:String, port:Int): Unit ={
    addProperty("Host", host)
    addProperty("Port", port)
  }

  def getHostAndPort: (Option[String], Option[Int]) ={
    get[String]("Host") -> getInt("Port")
  }

  def setReceiverNames(names:Iterable[String]): Unit ={
    addProperty("ReceiverNames", names.mkString(","))
  }

  def getReceiverNames:Option[Iterable[String]] = {
    get[String]("ReceiverNames").map(_.split(",").toIterable)
  }

}

/**
  * Akka address book
  * */

class AkkaAddressBook{

  private var addresses:Map[String,AddressItem] = Map.empty[String,AddressItem]

  def addressNumber:Long = addresses.size

  def addAddress(componentId:String, componentName:String,
                 componentType:String, address:String): Unit ={
    addresses += (componentId -> AddressItem(componentName, componentType, address))
  }

  def addAddress(componentId:String, address: AddressItem): Unit = {
    addresses += (componentId -> address)
  }

  def getAddressById(componentId:String):Option[String] = {
    addresses.get(componentId).map(_.akkaAddress)
  }

  def getAddressByName(componentName:String):Option[String] = {
    addresses
      .find(_._2.componentName == componentName)
      .map(_._2.akkaAddress)
  }

  def getAddressByTypeFirst(componentType:String):Option[String] = {
    addresses
      .find(_._2.componentType == componentType)
      .map(_._2.akkaAddress)
  }

  def getAddressByType(componentType:String):Iterator[String] = {
    addresses
      .filter(_._2.componentType == componentType)
      .map(_._2.akkaAddress).toIterator
  }

  def getAllAddress: Map[String, AddressItem] = addresses

  def removeAddress(label:String):Unit = {
    addresses -= label
  }

  def replace(other:AkkaAddressBook):Unit = addresses = other.getAllAddress

  def merge(other:AkkaAddressBook):Unit = {
    other.getAllAddress.foreach{
      case(label, address) =>
        addAddress(label, address)
    }
  }
}

case class AddressItem(componentName:String, componentType:String, akkaAddress:String)