package huanhuan.webcollect.browerdriver

import huanhuan.logs.CLog
import org.apache.log4j.{Level, Logger}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.{SgmlPage, WebClient, WebClientOptions}
import java.nio.charset.StandardCharsets


class HtmlUnitDriver extends CLog{

  val loggerHtml: Logger = Logger.getLogger("com.gargoylesoftware.htmlunit")
  loggerHtml.setLevel(Level.OFF)

  var timeOut:Int = 20000

  def getPageWithJS(url:String):Option[Document] = {
    getHtmlUnit(url).map{
      content =>
        Jsoup.parse(content,url)
    }
  }

  def getPageWithoutJS(url:String):Option[Document] = {
    getHtmlUnit(url,false).map{
      content =>
        Jsoup.parse(content,url)
    }
  }

  def setTimeOut(time:Int):this.type = {
    timeOut = math.max(100,time)
    this
  }

  private def getHtmlUnit(url:String,jsStart:Boolean = true):Option[String] = {
    val webClient: WebClient = new WebClient()
    val webOption: WebClientOptions = webClient.getOptions
    webOption.setCssEnabled(false)
    webOption.setThrowExceptionOnFailingStatusCode(false)
    webOption.setThrowExceptionOnScriptError(false)
    webOption.setTimeout(timeOut)
    webClient.addRequestHeader("content-type","text/html;charset=utf-8")

    webOption.setJavaScriptEnabled(jsStart)
    webOption.setTimeout(timeOut)
    val t1 = System.currentTimeMillis()
    try{
      webClient.getPage[HtmlPage](url)
      webClient
        .getCurrentWindow
        .getEnclosedPage match {
        case p:SgmlPage =>
          Some(p.asXml())
        case other =>
          val str = other.getWebResponse.getContentAsString(StandardCharsets.UTF_8)
          Some(str)
      }
    }catch{
      case e:Exception =>
        error(s"failed get url $url, ${e.getMessage}")
        None
    }finally {
      webClient.close()
      info(s"webclient close $url  cost time:${System.currentTimeMillis() - t1}")
    }
  }

  def getHtmlPage(url:String,jsStart:Boolean = true):Option[HtmlPage] = {
    val webClient: WebClient = new WebClient()
    val webOption: WebClientOptions = webClient.getOptions
    webOption.setCssEnabled(false)
    webOption.setThrowExceptionOnFailingStatusCode(false)
    webOption.setThrowExceptionOnScriptError(false)
    webOption.setTimeout(timeOut)
    webClient.addRequestHeader("content-type","text/html;charset=utf-8")

    webOption.setJavaScriptEnabled(jsStart)
    webOption.setTimeout(timeOut)
    val t1 = System.currentTimeMillis()
    try{
      Some(webClient.getPage[HtmlPage](url))
    }catch{
      case e:Exception =>
        error(s"failed get url $url, ${e.getMessage}")
        None
    }finally {
      webClient.close()
      info(s"webclient close $url  cost time:${System.currentTimeMillis() - t1}")
    }
  }


}
