import akka.actor.{ActorSystem, Props}
import huanhuan.webcollect.linkserver.LinkSeverUnit
import spidertest.{SimpleSpider, SimpleSpiderMaster}

object ClusterTest {

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("sys_")
    sys.actorOf(Props(classOf[SimpleSpiderMaster]), "master")
    sys.actorOf(Props(classOf[SimpleSpider], "Spider1"), "spider")
   // sys.actorOf(Props(classOf[SimpleSpider], "Spider2"), "spider1")
    sys.actorOf(LinkSeverUnit.createRedisServer("LinkServer"), "LinkServer")
    sys.actorOf(LinkSeverUnit.createRedisServer("LinkServer1"), "LinkServer1")
  }


}
