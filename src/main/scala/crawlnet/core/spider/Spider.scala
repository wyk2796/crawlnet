package crawlnet.core.spider

trait Spider {

  def nextUrl():String

  def urlFilter(url:String):Boolean



}

trait BrowersDriver{

  def getPage(url:String):String

}

