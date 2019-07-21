package huanhuan.webcollect.linkserver

import huanhuan.webcollect.linkserver.client.DataBaseClient

trait LinkSever{

  val DBC:DataBaseClient

  def saveLinks(links:String*):Unit

  def getLinks(number:Int):Iterable[String]

}



object LinkMessage{

  case class RequestLink(number:Int, returnAddress:String)
  case class NextLinks(links:TraversableOnce[String])
  case class NewLinks(links:TraversableOnce[String])


}