package crawlnet.linkserver

import crawlnet.core.component.cluster.AkkaClusterUnit

trait LinkSever extends AkkaClusterUnit{

  def saveLinks(links:TraversableOnce[String]):Unit

  def getLinks(number:Int):Iterable[String]

}


trait DataBaseClient{

  def insertLinks(links:TraversableOnce[String])

  def getLinks(number:Int):Iterable[String]

  def getLink:String

  def isUsedLink(link:String):Boolean

  def recordUsedLink(code: String)

  def removeUsedLink(code: String)

  def numberOfUsedLinks():Long

  def numberOfLinks():Long
}

object LinkMessage{

  case class RequestLink(number:Int, returnAddress:String)
  case class NextLinks(links:TraversableOnce[String])
  case class NewLinks(links:TraversableOnce[String])


}