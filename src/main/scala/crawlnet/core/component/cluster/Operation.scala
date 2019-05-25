package crawlnet.core.component.cluster

import crawlnet.core.component.{AkkaParameter, AkkaUnit}
import crawlnet.core.component.cluster.ClusterMessage.MasterReceive.{ResponseForGreet, UnitRegister}
import crawlnet.core.component.cluster.ClusterMessage.MasterRequest.Greet
import crawlnet.core.util

sealed trait Operation

/** slave components's handler of messages
  * */
trait GreetOp extends Operation{
  self:AkkaUnit =>

  receiveAdd("Greet", greetMsg)

  def responseGreet(): Unit ={
    ResponseForGreet(componentId, param)
  }

  def greetMsg:Receive = {
    case Greet() =>
      responseGreet()
  }
}

/**
  * Master Register Operation
  */
trait RegisterOp{
  self:AkkaUnit =>
  var componentTable:Map[String, AkkaParameter] = Map[String, AkkaParameter]()

  receiveAdd("Register", registerReceive)
  def register(id:String, name:String, p:AkkaParameter): Unit = {
    p.addProperty("Name", name)
    p.getAkkaAddress.foreach(akkaAddressBook.addAddress(id,_))
    componentTable += (id -> param)
    info(s"Register component: Id: $id, Name: $name")
  }

  def registerReceive:Receive = {
    case UnitRegister(id, name, p:AkkaParameter) =>
      register(id, name, p)
  }

}

/**
  * Master monitor operation
  * */
trait MasterMonitorOp extends Operation {
  self:AkkaClusterMaster =>

  receiveAdd("MasterMonitor", monitorReceive)
  def respForGreet(id:String, p:AkkaParameter, timeStamp:Long): Unit ={
    val componentParam = componentTable.get(id).map{
      param =>
        param.merge(p)
        param.addProperty("last_connected_time", util.convertTimeStampToDate(timeStamp))
        param
    }
    componentTable += (id -> componentParam)
  }



  def monitorReceive:Receive = {
    case ResponseForGreet(id, data:AkkaParameter, timeStamp) =>
      respForGreet(id, data, timeStamp)
  }
}

