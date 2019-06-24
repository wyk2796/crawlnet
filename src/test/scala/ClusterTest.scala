import akka.actor.{ActorSystem, Props}
import crawlnet.GlobalConfiguration
import crawlnet.core.spider.browerdriver.ChromeDriver
import crawlnet.linkserver.LinkMessage.NewLinks
import crawlnet.linkserver.LocalMemoryLinkSever
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import spidertest.{CrawlTask, SimpleSpider, SimpleSpiderMaster}
import scala.collection.convert.ImplicitConversionsToScala._

object ClusterTest {

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("sys_")
    sys.actorOf(Props(classOf[SimpleSpiderMaster]), "master")
    sys.actorOf(Props(classOf[SimpleSpider]), "spider")
    sys.actorOf(Props(classOf[LocalMemoryLinkSever], "LinkServer"), "LinkServer")
  }

//  def main(args: Array[String]): Unit = {
//
//    val driver: ChromeDriver = ChromeDriver.apply()
//    val seed = GlobalConfiguration.getString("CrawlTask.seed")
//    val page: Document = Jsoup.parse(driver.getPage(seed), seed)
//    val links = page.select("a[href]")
//      .map(_.attr("abs:href"))
//    println(s"collect links: ${links.length}")
//    links.foreach(println)
//    driver.close()
//  }

}
