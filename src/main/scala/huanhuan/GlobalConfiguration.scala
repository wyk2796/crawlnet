package huanhuan

import com.typesafe.config.{Config, ConfigFactory, ConfigList}

object GlobalConfiguration {

  val config: Config = ConfigFactory.load("configuration.conf")

  def getString(key:String):String = config.getString(key)
  def getDouble(key:String):Double = config.getDouble(key)
  def getInt(key:String):Int = config.getInt(key)
  def getBoolean(key:String):Boolean = config.getBoolean(key)
  def getLong(key:String):Long = config.getLong(key)
  def getBytes(key:String):Long = config.getBytes(key)
  def getList(key:String): ConfigList = config.getList(key)
  def getConfig(key:String):Config = config.getConfig(key)
}
