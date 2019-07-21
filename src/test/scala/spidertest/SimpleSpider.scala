package spidertest

import akka.actor.Props
import huanhuan.GlobalConfiguration
import huanhuan.cluster.component.AkkaClusterMaster
import huanhuan.cluster.operation.ThreadBasic
import huanhuan.webcollect.browerdriver.{ChromeDriver, DownloadDriver, HtmlUnitDriver}
import huanhuan.webcollect.linkserver.LinkMessage.{NewLinks, RequestLink}
import huanhuan.webcollect.spider.Worker.WorkTask
import huanhuan.webcollect.spider.{SpiderServer, Worker}

import scala.collection.convert.ImplicitConversionsToScala._

class SimpleSpider(name:String) extends SpiderServer with ThreadBasic{
  override val componentName: String = name
  override val componentType: String = "Spider"
  override val componentId: String = (componentName + componentType).hashCode.toString
  //val driver: ChromeDriver = ChromeDriver.apply()
  val htmlDriver = new HtmlUnitDriver
  val seedLink:String = GlobalConfiguration.getString("CrawlTask.seed")
  initialUnit()
  register()
  createWorkers(5, Props[SpiderCollect])
  initialWorker(Map("browser_driver" -> htmlDriver))

  execute(initialSpider)
  def initialSpider(): Unit = {
    while(akkaAddressBook.getAddressByTypeFirst("LinkServer").isEmpty){
      Thread.sleep(1000)
      warn(s"[$componentId, $componentName]: not get LinkServer address!")
    }
    insertTask(WorkTask(CrawlTask(seedLink,
      akkaAddressBook.getAddressByTypeRandom("LinkServer").get)))
    beginWork()
    sendMessageByType("LinkServer", RequestLink(workLen * 10, param.getAkkaAddress))
  }


  def generateTask(link:Any): WorkTask =
    WorkTask(CrawlTask(link.toString,
    akkaAddressBook.getAddressByTypeRandom("LinkServer").get))
}

class SimpleSpiderMaster extends AkkaClusterMaster {
  override val componentName: String = "SpiderMaster"
  override val componentType: String = "master"
  override val componentId: String = (componentName + componentType).hashCode.toString
  initialUnit()
}

class SpiderCollect extends Worker{
  val path ="E:\\temp\\picture"
  var taskNum: Int = 1
  var totalTime:Long = 0
  //val driver = ChromeDriver.apply()
  override def runTask(task: Any): Unit = task match {
    case CrawlTask(link, address) =>
      val time1 = System.currentTimeMillis()
      val driver: HtmlUnitDriver = workParam.get[HtmlUnitDriver]("browser_driver").get
      val pageContent = driver.getPageWithoutJS(link)
      pageContent match {
        case None => warn(s"[Worker] Can't get content from $link")
        case Some(page) =>

          val costTime = System.currentTimeMillis() - time1
          val links = page.select("a[href]")
            .map(_.attr("abs:href"))
          val imageLinks = page.select("img[src]")
            .map(_.attr("abs:src"))
          info(s"collect links: ${links.length}")
          imageLinks.foreach(
            DownloadDriver.downloadWithFilter(_, path, (math.random() * 1000).toInt.toString + ".jpg"){
              (len, ptype) =>
                len < 6000 ||  ptype != "image/jpeg"
            }
          )
          totalTime += costTime
          info(s"[Worker] cost ${costTime}ms to get page! average ${totalTime / taskNum}")
          context.actorSelection(address) ! NewLinks(links)
          taskNum += 1
          info(s"working completed task num:$taskNum")
          //Thread.sleep(1000)
      }
  }


}

case class CrawlTask(taskLink:String, sinkAkkaAddress:String)