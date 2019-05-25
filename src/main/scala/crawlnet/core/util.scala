package crawlnet.core

import java.text.SimpleDateFormat
import java.util.Date

object util {

  private lazy val format = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss")
  def convertTimeStampToDate(timeStamp:Long): String ={
    format.format(new Date(timeStamp))
  }


}

