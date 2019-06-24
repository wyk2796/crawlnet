package crawlnet.core.component.cluster

import akka.util.Timeout
import crawlnet.core.component.{AkkaAddressBook, AkkaParameter, AkkaUnit}
import crawlnet.core.component.cluster.ClusterMessage.MasterReceive.{ResponseForGreet, ResponseForRegister, UnitRegister, Unregister}
import crawlnet.core.component.cluster.ClusterMessage.MasterRequest.Greet
import crawlnet.core.util

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Failure

sealed trait Operation


trait ThreadBasic{
  self:AkkaUnit =>
  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val exc:ExecutionContextExecutor = context.dispatcher

  def execute[A](fun:A => Unit, arg:A): Unit = {
    Future {
      fun(arg)
    } onComplete{
      case Failure(e: Throwable) =>
        error("Execute error: " + e.fillInStackTrace())
      case _ => info("complete")
    }
  }

  def execute(fun:() => Unit): Unit ={
    Future {
      fun()
    } onComplete{
      case Failure(e: Throwable) =>
        error("Execute error: " + e.fillInStackTrace())
      case _ => info("complete")
    }
  }
}
/** slave components's handler of messages
  * */
trait UnitSynchronizationOp extends Operation{
  self:AkkaUnit =>
  receiveAdd("SynchronizationOp", synchronizationReceive)

  def responseGreet(params:Map[String,Any]): Unit = {
    params.foreach{
      case (_, book:AkkaAddressBook) =>
        akkaAddressBook.replace(book)
      case _ =>
    }
//    info(s"[$componentId, $componentName]: get greeting from master")
    sendMessageByType("master", ResponseForGreet(componentId, param))
  }

  def synchronizationReceive:Receive = {
    case Greet(params) => responseGreet(params)
  }
}

trait UnitRegisterOp extends Operation {
  self:AkkaUnit =>

  receiveAdd("Register", RegisterReceive)
  def register(): Unit = {
    sendMessageByType("master",
      UnitRegister(componentId, param))
  }
  def unregister(): Unit ={
    sendMessageByType("master", componentId)
    hookAfterUnregister()
  }

  def hookAfterRegister(params:Map[String,Any]): Unit = {
    params.foreach{
      case (_, book:AkkaAddressBook) =>
        akkaAddressBook.replace(book)
      case _ =>
    }
  }

  def hookAfterUnregister(): Unit = {}

  def RegisterReceive:Receive = {
      case ResponseForRegister(params) =>
        info(s"[$componentId, $componentName] register successful")
        hookAfterRegister(params)
  }
}

/**
  * Master Register Operation
  */
trait MasterRegisterOp extends Operation {
  self:AkkaClusterMaster =>
  var componentTable:Map[String, AkkaParameter] = Map[String, AkkaParameter]()
  receiveAdd("Register", registerControllerReceive)

  def register(id:String, p:AkkaParameter): Unit = {
    val rName = p.getComponentName
    val rType = p.getComponentType
    val rAddress = p.getAkkaAddress
    akkaAddressBook.addAddress(id, rName, rType, rAddress)
    componentTable += (id -> p)
    info(s"Register [componentId: $id, Name: $rName, Type: $rType]")
    sender() ! ResponseForRegister(Map("AddressBook" -> akkaAddressBook))
  }

  def unregister(id:String):Unit = {
    akkaAddressBook.removeAddress(id)
    componentTable -= id
  }

  def registerControllerReceive:Receive = {
    case UnitRegister(id, p:AkkaParameter) =>
      register(id, p)

    case Unregister(id) =>
      unregister(id)
  }
}

/**
  * Master monitor operation
  * */
trait MasterSynchronizationOp extends ThreadBasic with Operation {
  self:AkkaClusterMaster =>

  receiveAdd("MasterSynchronization", synchronizationReceive)
  execute[Int](sendGreet, 5)
  def buildSynchronizationContent:Map[String,Any] = {
    Map("AddressBook" -> akkaAddressBook)
  }

  def sendGreet(interval:Int): Unit = {
    while(true){
      akkaAddressBook.getAllAddress.foreach{
        case (_, address) if address.componentType != "master" =>
          context.actorSelection(address.akkaAddress) ! Greet(buildSynchronizationContent)
        case _ =>
      }
      Thread.sleep(interval * 1000)
    }
  }

  def respForGreet(id:String, p:AkkaParameter, timeStamp:Long): Unit ={
    val componentParam = componentTable.get(id).map{
      param =>
        param.merge(p)
        param.addProperty("last_connected_time", util.convertTimeStampToDate(timeStamp))
        param
    }.get
//    info(s"[$componentId, $componentName]: get greet back from $id, ${p.getComponentName}")
    componentTable += (id -> componentParam)
  }

  def synchronizationReceive:Receive = {
    case ResponseForGreet(id, data:AkkaParameter, timeStamp) =>
      respForGreet(id, data, timeStamp)
  }
}

