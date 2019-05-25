package crawlnet.core.worker

import akka.actor.Actor

trait Worker[I,O] extends Actor{
  import Worker._
  val id:Int
  def execute(data:I):O
  def handlerResult(data:I, result:O)

  override def receive: Receive = {
    case data:I =>
      val result = execute(data)
      handlerResult(data, result)
    case STOP => context stop self
  }
}

object Worker{
  val STOP = "STOP"
}
