package huanhuan.webcollect.linkserver.client

/**
  * the implement of database client for Linkserver.
  * */
trait DataBaseClient{

  def insertLinks(links:String *)

  def getLinks(number:Int):Iterable[String]

  def getLink:String

  def isUsedLink(link:String):Boolean

  def recordUsedLink(code: String*)

  def removeUsedLink(code: String)

  def numberOfUsedLinks():Long

  def numberOfLinks():Long
}
