package huanhuan.webcollect.browerdriver

import java.io.{File, FileOutputStream}
import java.net.{URL, URLConnection}

import huanhuan.logs.CLog

object DownloadDriver extends CLog{

  def download(urlString:String,savePath:String,fileName:String):Boolean = {
    val url: URL = new URL(urlString)
    val con: URLConnection = url.openConnection()
    val contentLen: Int = con.getContentLength
    con.setConnectTimeout(20000)
    con.setReadTimeout(1000 * 180)
    val is = con.getInputStream
    val bs = new Array[Byte](1024 * 1024)
    val sf = new File(savePath)
    if(!sf.exists()){
      sf.mkdirs()
    }
    val os = new FileOutputStream(sf.getPath+"/"+fileName)

    try{
      info(s"[Download] downloading $fileName, size: $contentLen, type: ${con.getContentType}")
      var len = is.read(bs)
      while (len != -1) {
        os.write(bs, 0, len)
        len = is.read(bs)
      }
      info("[Download] completed downloading " + fileName)
      os.close()
      is.close()
      true
    }catch {
      case e:Exception =>
        error(s"[Download] download $fileName have an error")
        error(e)
        false
    }finally {
      is.close()
      os.close()
    }
  }


  def downloadWithFilter(urlString:String,savePath:String,fileName:String)
                        (filter: (Long, String) => Boolean): Boolean ={
    val url: URL = new URL(urlString)
    val con: URLConnection = url.openConnection()
    val contentLen: Int = con.getContentLength
    val contentType:String = con.getContentType
    con.setConnectTimeout(20000)
    con.setReadTimeout(1000 * 180)
    if(filter(contentLen, contentType))
      return false
    val is = con.getInputStream
    val bs = new Array[Byte](1024 * 1024)
    val sf = new File(savePath)
    if(!sf.exists()){
      sf.mkdirs()
    }
    val os = new FileOutputStream(sf.getPath+"/"+fileName)
    try{
      info(s"[Download] downloading $fileName, size: $contentLen, type: $contentType")
      var len = is.read(bs)
      while (len != -1) {
        os.write(bs, 0, len)
        len = is.read(bs)
      }
      info("[Download] completed downloading " + fileName)
      os.close()
      is.close()
      true
    }catch {
      case e:Exception =>
        error(s"[Download] download $fileName have an error")
        error(e)
        false
    }finally {
      is.close()
      os.close()
    }
  }
}
