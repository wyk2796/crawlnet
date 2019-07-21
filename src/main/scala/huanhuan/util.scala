package huanhuan

import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.config.Config

object util {

  /***
    * transform s to md5 form.
    */
  def md5HashString(s: String): String = {
    import java.security.MessageDigest
    import java.math.BigInteger
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def convertTimeStampToDate(timeStamp:Long, patten:String="yyyy-MM-DD HH:mm:ss"): String ={
    val format = new SimpleDateFormat(patten)
    format.format(new Date(timeStamp))
  }


  def configDefault[T](config:Config, key:String, defaultValue:T): T ={
    if(!config.hasPath(key))
      defaultValue
    else
      config.getAnyRef(key).asInstanceOf[T]
  }

  def configOption[T](config:Config, key:String): Option[T] ={
    if(!config.hasPath(key))
      None
    else
      Some(config.getAnyRef(key).asInstanceOf[T])
  }
}
