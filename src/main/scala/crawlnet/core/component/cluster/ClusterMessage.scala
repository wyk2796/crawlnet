package crawlnet.core.component.cluster

import crawlnet.core.component.Parameter

object ClusterMessage {
  sealed trait Message

  object MasterReceive{
    case class UnitRegister(componentId:String, componentName: String, param:Parameter) extends Message
    case class UnRegister(componentId:String) extends Message

    case class ResponseForGreet(componentId:String,
                                data:Parameter,
                                timeStamp:Long = System.currentTimeMillis()) extends Message
  }

  object MasterRequest{
    case class Greet() extends Message
    case class Running()extends Message
    case class Pause() extends Message
    case class Resume() extends Message
    case class Stop() extends Message
  }

  object ComponentReceive{

  }

  object ComponentRequest{

  }

}
