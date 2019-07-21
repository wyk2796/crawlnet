package huanhuan.cluster.component

import huanhuan.cluster.parameter.Parameter

object ClusterMessage {
  sealed trait Message

  object MasterReceive{
    case class UnitRegister(componentId:String, param:Parameter) extends Message
    case class Unregister(componentId:String) extends Message
    case class ResponseForRegister(params:Map[String,Any]) extends Message
    case class ResponseForGreet(componentId:String,
                                data:Parameter,
                                timeStamp:Long = System.currentTimeMillis()) extends Message
  }

  object MasterRequest{
    case class Greet(params:Map[String,Any]) extends Message
//    case class Running()extends Message
//    case class Pause() extends Message
//    case class Resume() extends Message
//    case class Stop() extends Message
  }

  object ComponentReceive{

  }

  object ComponentRequest{

  }

}
