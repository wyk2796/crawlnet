package spidertest

import crawlnet.GlobalConfiguration
import crawlnet.core.component.cluster.{AkkaClusterMaster, ThreadBasic}
import crawlnet.core.spider.Worker.WorkTask
import crawlnet.core.spider.{SpiderServer, Worker}
import crawlnet.core.spider.browerdriver.ChromeDriver
import crawlnet.linkserver.LinkMessage.NewLinks
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.convert.ImplicitConversionsToScala._

class SimpleSpider extends SpiderServer with ThreadBasic{
  override val componentName: String = "Spider"
  override val componentType: String = "Spider"
  override val componentId: String = (componentName + componentType).hashCode.toString
  val driver: ChromeDriver = ChromeDriver.apply()
  val seedLink:String = GlobalConfiguration.getString("CrawlTask.seed")
  initial()
  register()
  createWorkWithClasspath(20, "spidertest.SpiderCollect")
  initialWorker(Map("browser_driver" -> driver))

  execute(initialSpider)
  def initialSpider(): Unit = {
    while(akkaAddressBook.getAddressByTypeFirst("LinkServer").isEmpty){
      Thread.sleep(1000)
      warn(s"[$componentId, $componentName]: not get LinkServer address!")
    }
    initialTasks(List(WorkTask(CrawlTask(seedLink,
      akkaAddressBook.getAddressByTypeFirst("LinkServer").get))))
    assignTask()
  }

  def createTask(link:String): WorkTask =
    WorkTask(CrawlTask(link,
    akkaAddressBook.getAddressByTypeFirst("LinkServer").get))
}

class SimpleSpiderMaster extends AkkaClusterMaster {
  override val componentName: String = "SpiderMaster"
  override val componentType: String = "master"
  override val componentId: String = (componentName + componentType).hashCode.toString
  initial()
}

class SpiderCollect extends Worker{

  var taskNum: Int = 0
  override def runTask(task: Any): Unit = task match {
    case CrawlTask(link, address) =>
      val driver: ChromeDriver = workParam.get[ChromeDriver]("browser_driver").get
      val page: Document = Jsoup.parse(driver.getPage(link), link)
      val links = page.select("a[href]")
        .map(_.attr("abs:href"))
      info(s"collect links: ${links.length}")
//      links.foreach(info)
      context.actorSelection(address) ! NewLinks(links)
      driver.close()
      Thread.sleep(1000)
      taskNum += 1
      info(s"working completed task num:$taskNum")
  }


}

case class CrawlTask(taskLink:String, sinkAkkaAddress:String)