package huanhuan.cluster.operation

import huanhuan.cluster.component.{AkkaClusterMaster, AkkaParameter}
import huanhuan.cluster.component.ClusterMessage.MasterReceive.{ResponseForGreet, ResponseForRegister, UnitRegister, Unregister}
import huanhuan.cluster.component.ClusterMessage.MasterRequest.Greet
import huanhuan.util

trait AkkaMasterOperation extends AkkaUnitOperation

/**
  * Master Register Operation
  */
trait MasterRegisterOp extends AkkaMasterOperation {
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
    info(s"Unregister [componentId: $id]")
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
trait MasterSynchronizationOp extends ThreadBasic with AkkaMasterOperation {
  self:AkkaClusterMaster =>
  receiveAdd("MasterSynchronization", synchronizationReceive)
  executeWithArgs(5){
     s =>
       while(true){
         akkaAddressBook.getAllAddress.foreach{
           case (_, address) if address.componentType != "master" =>
             context.actorSelection(address.akkaAddress) ! Greet(buildSynchronizationContent)
           case _ =>
         }
         Thread.sleep(s * 5000)
         cleanUnconnectedUnit()
       }
  }

  def cleanUnconnectedUnit(): Unit ={
    componentTable.filter{
      case(_, unitParam) =>
        unitParam.get[Long]("last_record_time") match {
          case Some(t) =>
            (System.currentTimeMillis() - t) > 180000
          case None => false
        }
    }.foreach(per => unregister(per._1))
  }

  def buildSynchronizationContent:Map[String,Any] = {
    Map("AddressBook" -> akkaAddressBook)
  }


  def respForGreet(id:String, p:AkkaParameter, timeStamp:Long): Unit ={
    val componentParam = componentTable.get(id).map{
      param =>
        param.merge(p)
        param.addProperty("last_connected_time", util.convertTimeStampToDate(timeStamp))
        param.addProperty("last_record_time", System.currentTimeMillis())
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

