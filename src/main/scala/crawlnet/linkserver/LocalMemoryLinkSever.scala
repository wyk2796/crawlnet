package crawlnet.linkserver

import crawlnet.core.component.cluster.AkkaClusterUnit
import crawlnet.linkserver.LinkMessage.{NewLinks, NextLinks, RequestLink}
import scala.collection.immutable.HashSet
import scala.collection.immutable.Queue

class LocalMemoryLinkSever(name:String) extends AkkaClusterUnit with LinkSever{

  override val componentName: String = name
  override val componentId: String = componentName.hashCode().toString
  override val componentType: String = "LinkServer"
  val LMC:LocalMemoryClient = new LocalMemoryClient()
  receiveAdd("LinkServer", linkSeverReceive)


  param.setComponentName(componentName)
  param.setComponentType(componentType)
  param.setComponentId(componentId)
  akkaAddressBook.addAddress(componentId, componentName, componentType, param.getAkkaAddress)
  info(s"[$componentId, $componentName, $componentType]: server is running, address:${param.getAkkaAddress}")
  info(s"[$componentId, $componentName, $componentType]: register receiver:" + getReceiverNames.mkString(","))
  register()


  override def getLinks(number:Int = 5): Iterable[String] = {
    LMC.getLinks(number)
  }

  override def saveLinks(links: TraversableOnce[String]): Unit = {
    LMC.insertLinks(links)
  }

  private def updateParam(): Unit ={
    param.addProperty("LinkNumInPool", LMC.numberOfLinks())
    param.addProperty("UsedLinkNum", LMC.numberOfUsedLinks())
  }

  def linkSeverReceive:Receive = {
    case NewLinks(links) =>
      saveLinks(links)
      updateParam()
    case RequestLink(num, address) =>
      context.actorSelection(address) ! NextLinks(getLinks(num))
      updateParam()
  }
}

class LocalMemoryClient extends DataBaseClient{
  import crawlnet.util.md5HashString

  var linksPool: Queue[String] = Queue.empty[String]
  var md5CodePool = HashSet.empty[String]

  def insertLinks(links:TraversableOnce[String]): Unit ={
    links.foreach{
      link =>
        val lcode = md5HashString(link)
        if(!isUsedLink(lcode)){
          linksPool = linksPool.enqueue(link)
          recordUsedLink(lcode)
        }
    }
  }

  def getLinks(number:Int = 1):Iterable[String] = {
    for(_ <- 0 until number) yield getLink
  }

  def getLink:String = {
    val elem = linksPool.dequeue
    linksPool = elem._2
    elem._1
  }

  def isUsedLink(code:String):Boolean = {
    md5CodePool.contains(code)
  }

  def recordUsedLink(code: String): Unit ={
    md5CodePool = md5CodePool + code
  }

  def removeUsedLink(code: String): Unit = {
    md5CodePool -= code
  }

  def numberOfUsedLinks():Long = md5CodePool.size

  def numberOfLinks():Long = linksPool.length

}