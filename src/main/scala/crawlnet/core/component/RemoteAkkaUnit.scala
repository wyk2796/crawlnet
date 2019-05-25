package crawlnet.core.component

import akka.actor.{ActorRef, ActorSelection}
import crawlnet.core.Parameter
import crawlnet.core.message.AkkaMessage.MasterReceive.ResponseForGreet
import crawlnet.core.message.AkkaMessage.MasterRequest.{Greet, Pause, Resume, Running, Stop}

abstract class RemoteAkkaUnit(override val componentId:String,
                     override val componentName:String,
                     override val componentHost:AkkaHost,
                     val masterAkkaAddress: ActorSelection) extends AkkaUnit {

  val maxWorkers:Int = 20
  componentStatus = "Running"
  var Workers: Map[Int, ActorRef] = Map[Int, ActorRef]()
  val parameter: Parameter = new Parameter

  def createWorker():ActorRef
  def initialWorker(): Unit ={
    for(i <- 0 until maxWorkers){
      Workers += i -> createWorker()
    }
  }
  def launch(): Unit
  def pause():Unit
  def resume():Unit
  def stop():Unit = {
    Workers.mapValues(_ ! "STOP")
  }

  def MasterCommunicate:Receive = {
    case Greet() =>
      sender() ! ResponseForGreet(componentId, componentName, parameter.getParameters)
    case Running() =>
      launch()
    case Pause() =>
      pause()
    case Resume() =>
      resume()
    case Stop() =>
      context stop self
      stop()
  }
}


