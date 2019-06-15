package crawlnet.linkserver

import crawlnet.core.component.cluster.AkkaClusterUnit

trait LinkSever extends AkkaClusterUnit{

  def saveLinks(links:String):Unit

  def getLinks:Iterable[String]

}


trait DataBaseClient{

  def insertLinks(links:Traversable[String])

  def getLinks(number:Int):Iterable[String]

  def getLink:String

  def isUsedLink(link:String):Boolean

  def recordUsedLink(code: String)

  def removeUsedLink(code: String)

  def numberOfUsedLinks():Long

  def numberOfLinks():Long
}