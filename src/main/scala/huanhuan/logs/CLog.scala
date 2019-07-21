package huanhuan.logs

import org.apache.log4j.{DailyRollingFileAppender, Logger, PatternLayout}

trait CLog {

  var log_ :Logger = _

  def logger: Logger =
    if (log_ != null)
      log_
    else {
      log_ = Logger.getLogger(this.getClass.getSimpleName)
      val dailyFile = new DailyRollingFileAppender(new PatternLayout("%d %p [%c] - %m%n"),
        s"../logs/${this.getClass.getSimpleName.toLowerCase}.log", """'.'yyyy-MM-dd""")
      log_.addAppender(dailyFile)
      log_

    }

  def info(msg:Any): Unit ={
    logger.info(msg)
  }

  def warn(msg:Any):Unit ={
    logger.warn(msg)
  }

  def error(msg:Any): Unit ={
    logger.error(msg)
  }
}
