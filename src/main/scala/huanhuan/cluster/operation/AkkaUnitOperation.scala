package huanhuan.cluster.operation

import akka.util.Timeout
import huanhuan.cluster.component.AkkaUnit
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Failure
import scala.concurrent.duration._

trait AkkaUnitOperation extends Operation

trait ThreadBasic{
  self:AkkaUnit =>
  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val exc:ExecutionContextExecutor = context.dispatcher

  def executeWithArgs[A](arg:A)(fun:A => Unit): Unit = {
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
