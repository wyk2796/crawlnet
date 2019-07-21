package huanhuan.webcollect.browerdriver

import java.util

import huanhuan.GlobalConfiguration
import huanhuan.logs.CLog
import org.openqa.selenium.chrome
import org.openqa.selenium.Proxy
import scala.collection.TraversableOnce
import scala.collection.convert.ImplicitConversionsToScala._

class ChromeDriver(proxyIp:Option[String], param:TraversableOnce[String]) extends CLog{

  val driverPath: String = GlobalConfiguration.getString("Chrome.path")
  System.setProperty("webdriver.chrome.driver", driverPath)
  val chromeOpts = new chrome.ChromeOptions()
  chromeOpts.addArguments("window-size=1200x600")

  //not load picture and headless mode.
  val prefs = new util.HashMap[String,Object]()
  prefs.put("profile.managed_default_content_settings.images", 2.asInstanceOf[Object])
  chromeOpts.setExperimentalOption("prefs", prefs)
  chromeOpts.setHeadless(true)

  param.foreach(chromeOpts.addArguments(_))
  proxyIp.map{ ip=>val p = new Proxy; p.setHttpProxy(ip) }.foreach(chromeOpts.setProxy)
  var driver: chrome.ChromeDriver = new chrome.ChromeDriver(chromeOpts)
  def createDriver(): chrome.ChromeDriver ={
    new chrome.ChromeDriver(chromeOpts)
  }

  def getPage(url: String): Option[String] = {
    val page = if(connected(url))
      Some(driver.getPageSource)
    else
      None
    page
  }

  def connected(url:String, retryTime:Int = 3): Boolean = {
    try{
      driver.get(url)
      true
    }catch {
      case e:Exception =>
        error(s"[Chrome] Chrome have an error when open the url: $url")
        error(e)
        driver.quit()
        driver = createDriver()
        if (retryTime >=0) connected(url, retryTime - 1)
        else false
    }
  }

  def close(): Unit ={
    driver.close()
  }

  def quiet(): Unit ={
    driver.quit()
  }

}

object ChromeDriver{

  def apply(): ChromeDriver = new ChromeDriver(None, List.empty[String])

  def apply(proxy: Option[String],
            param: TraversableOnce[String]): ChromeDriver = new ChromeDriver(proxy, param)

}
