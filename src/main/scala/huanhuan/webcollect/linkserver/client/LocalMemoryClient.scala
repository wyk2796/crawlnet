package huanhuan.webcollect.linkserver.client


import scala.collection.immutable.{HashSet, Queue}

class LocalMemoryClient extends DataBaseClient{
  import huanhuan.util.md5HashString

  var linksPool: Queue[String] = Queue.empty[String]
  var md5CodePool = HashSet.empty[String]

  def insertLinks(links:String *): Unit ={
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

  def recordUsedLink(code: String*): Unit ={
    md5CodePool = md5CodePool ++ code
  }

  def removeUsedLink(code: String): Unit = {
    md5CodePool -= code
  }

  def numberOfUsedLinks():Long = md5CodePool.size

  def numberOfLinks():Long = linksPool.length

}
