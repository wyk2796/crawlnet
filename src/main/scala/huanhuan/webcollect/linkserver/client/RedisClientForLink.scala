package huanhuan.webcollect.linkserver.client

import java.util

import huanhuan.GlobalConfiguration
import huanhuan.databaseclient.Redis.RedisClient
import huanhuan.util.md5HashString
import scala.collection.convert.ImplicitConversionsToScala._


class RedisClientForLink extends DataBaseClient{

  val client = RedisClient.apply(GlobalConfiguration.getConfig("RedisLink"))
  val LINKS_POOL = "LINKS_POOL"
  val ENCODE_POOL = "ENCODE_POOL"

  def insertLinks(links:String *): Unit = {
    val newLinks = (links zip links.map(md5HashString)).distinct
      .filter(x => !isUsedLink(x._2)).unzip
    if(newLinks._1.nonEmpty){
      recordLink(newLinks._1 :_*)
      recordUsedLink(newLinks._2 :_*)
    }
  }

  def getLinks(number:Int):Iterable[String] = {
    val mulTask = client.getInstance.multi()
    mulTask.lrange(LINKS_POOL, 0, number)
    mulTask.ltrim(LINKS_POOL, number + 1, -1)
    val result = mulTask.exec()(0).asInstanceOf[util.ArrayList[String]]
    if(result.isEmpty) Iterable.empty[String]
    else result
  }

  def getLink:String = {
    client.getInstance.rpop(LINKS_POOL)
  }

  def recordLink(links:String*): Unit ={
    client.getInstance.rpush(LINKS_POOL, links :_*)
  }

  def isUsedLink(link:String):Boolean = {
    client.getInstance.sismember(ENCODE_POOL, link)
  }

  def recordUsedLink(code: String*): Unit ={
    client.getInstance.sadd(ENCODE_POOL, code:_*)
  }

  def removeUsedLink(code: String): Unit ={
    client.getInstance.srem(ENCODE_POOL, code)
  }

  def numberOfUsedLinks():Long = {
    client.getInstance.scard(ENCODE_POOL)
  }

  def numberOfLinks():Long = {
    client.getInstance.llen(LINKS_POOL)
  }
}
