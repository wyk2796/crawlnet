package huanhuan.cluster.component

import akka.actor.Actor

trait AkkaReceiverManager {
  self:Actor =>

  var receiveSet:Map[String, Receive] = Map.empty[String, Receive]

  def receiveAdd(tag:String, other:Receive):Unit = {
    receiveSet += (tag -> other)
    context.become(receive)
  }

  def receiveRemove(tag:String):Unit = {
    receiveSet -= tag
    context.become(receive)
  }

  def getReceiverNames:Iterable[String] = receiveSet.keys

  def receive: Receive = receiveSet.values.reduce(_ orElse _)
}
