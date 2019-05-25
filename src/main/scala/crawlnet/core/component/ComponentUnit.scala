package crawlnet.core.component
import akka.actor.{ActorRef, ActorSystem, Address}
import akka.actor.Actor
import crawlnet.core.logs.CLog


/** Basic Component for distribute system
  * */
trait ComponentUnit {
  val componentId:String
  val componentName:String
  var componentStatus:String =_
}

private[component] trait AkkaUnit extends ComponentUnit with Actor with CLog{

  val param:AkkaParameter = new AkkaParameter
  val componentHost:AkkaHost = AkkaHost(context.system.settings.config.getString("akka.remote.netty.tcp.hostname"),
                                        context.system.settings.config.getInt("akka.remote.netty.tcp.port"))
  val akkaAddressBook = new AkkaAddressBook
  param.setHostAndPort(componentHost.hostname, componentHost.port)
  param.setAkkaAddress(componentHost.toAkkaAddress(self)(context.system))
  param.setReceiverNames(getReceiverNames)

  var receiveSet:Map[String, Receive] = Map.empty[String, Receive]
  final def receiveAdd(tag:String, other:Receive):Unit = {
    receiveSet += (tag -> other)
    context.become(receive)
  }

  final def receiveRemove(tag:String):Unit = {
    receiveSet -= tag
    context.become(receive)
  }

  final def getReceiverNames:Iterable[String] = receiveSet.keys

  final def receive: Receive = receiveSet.values.reduce(_ orElse _)
}


/** Host for Component
  * */
case class BasicHost(hostname:String, port:Int){
  override def toString:String = s"$hostname:$port"
}

case class LocalHost() extends BasicHost("127.0.0.1",2552)

case class AkkaHost(override val hostname:String, override val port:Int)
  extends BasicHost(hostname, port) with HostAkkaConvert

trait HostAkkaConvert{
  self:BasicHost=>
  def toAkkaAddress(actor: ActorRef)(implicit as:ActorSystem): String = {
    actor.path.toStringWithAddress(Address("tcp", as.name, hostname, port))
  }
}

/** Parameters in an component.
  * */
class AkkaParameter extends Parameter with AkkaParameterOp


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
}

trait AkkaParameterOp{
  self:Parameter =>

  def setAkkaAddress(address:String): Unit ={
    addProperty("Akka_Address", address)
  }

  def getAkkaAddress: Option[String] = {
    get[String]("Akka_Address")
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

  var addresses:Map[String,String] = Map.empty[String,String]

  def addAddress(label:String, address:String): Unit ={
    addresses += (label -> address)
  }

  def getAddress(label:String):Option[String] = {
    addresses.get(label)
  }

}