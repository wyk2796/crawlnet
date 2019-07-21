package huanhuan.cluster.operation

import huanhuan.cluster.component.{AkkaAddressBook, AkkaClusterUnit, AkkaUnit}
import huanhuan.cluster.component.ClusterMessage.MasterReceive.{ResponseForGreet, ResponseForRegister, UnitRegister}
import huanhuan.cluster.component.ClusterMessage.MasterRequest.Greet

trait ClusterUnitOperation extends AkkaUnitOperation

/** slave components's handler of messages
  * */
trait UnitSynchronizationOp extends ClusterUnitOperation{
  self:AkkaClusterUnit =>
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

trait UnitRegisterOp extends ClusterUnitOperation {
  self:AkkaClusterUnit =>

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
