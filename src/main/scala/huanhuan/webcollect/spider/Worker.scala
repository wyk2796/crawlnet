package huanhuan.webcollect.spider

import akka.actor.{Actor, ActorRef}
import huanhuan.cluster.parameter.Parameter
import huanhuan.logs.CLog

abstract class Worker extends Actor with CLog{
  import Worker._
  val workParam:Parameter = new Parameter{}

  def initial(param:Map[String,Any]): Unit = {
    param.foreach{
      case(key, value) =>
        workParam.addProperty(key, value)
    }
  }

  def runTask(task:Any)

  def requestTask(): Unit ={
    context.parent ! RequestTask(self)
  }

  def stop(): Unit = context stop self

  override def receive: Receive = {
    case Configuration(params) =>
      initial(params)
    case WorkTask(task) =>
      try{
        runTask(task)
      } catch {
        case e:Exception =>
          error(s"Worker have an error when run the task: $task")
          error(e)
      } finally {
        requestTask()
      }
    case WorkStart =>
      requestTask()
    case Idle(ts) =>
      Thread.sleep(ts)
      requestTask()
    case WorkStop =>
      stop()
  }
}

object Worker{
  trait WMessage
  case class Configuration(params:Map[String,Any]) extends WMessage
  case class WorkTask(task:Any) extends WMessage
  case class Idle(time:Long) extends WMessage
  case object WorkStop extends WMessage
  case object WorkStart extends WMessage
  case class RequestTask(ref:ActorRef) extends WMessage
}
