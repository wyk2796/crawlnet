package huanhuan.webcollect.linkserver

import huanhuan.cluster.component.AkkaClusterUnit
import LinkMessage.{NewLinks, NextLinks, RequestLink}
import akka.actor.Props
import huanhuan.webcollect.linkserver.client.{DataBaseClient, LocalMemoryClient, RedisClientForLink}


class LinkSeverUnit(name:String, dataBaseClient: DataBaseClient) extends AkkaClusterUnit with LinkSever{

  val componentName: String = name
  val componentId: String = componentName.hashCode().toString
  val componentType: String = "LinkServer"
  val DBC:DataBaseClient = dataBaseClient
  receiveAdd("LinkServer", linkSeverReceive)

  initialUnit()
  info(s"[$componentId, $componentName, $componentType]: server is running, address:${param.getAkkaAddress}")
  info(s"[$componentId, $componentName, $componentType]: register receiver:" + getReceiverNames.mkString(","))
  register()


  override def getLinks(number:Int = 5): Iterable[String] = {
    DBC.getLinks(number)
  }

  override def saveLinks(links: String*): Unit = {
    DBC.insertLinks(links :_*)
  }

  private def updateParam(): Unit ={
    param.addProperty("LinkNumInPool", DBC.numberOfLinks())
    param.addProperty("UsedLinkNum", DBC.numberOfUsedLinks())
  }

  def linkSeverReceive:Receive = {
    case NewLinks(links) =>
      saveLinks(links.toSeq :_*)
      updateParam()
    case RequestLink(num, address) =>
      context.actorSelection(address) ! NextLinks(getLinks(num))
      updateParam()
  }
}

object LinkSeverUnit{
  def apply(name: String, dataBaseClient: DataBaseClient): Props = Props(new LinkSeverUnit(name, dataBaseClient))
  def createLocalMemoryServer(name:String):Props = Props(new LinkSeverUnit(name, new LocalMemoryClient))
  def createRedisServer(name:String):Props = Props(new LinkSeverUnit(name, new RedisClientForLink))
}