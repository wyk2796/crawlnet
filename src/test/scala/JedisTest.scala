import huanhuan.GlobalConfiguration
import huanhuan.databaseclient.Redis.RedisClient

object JedisTest {

  def main(args: Array[String]): Unit = {
      val client = RedisClient.apply(GlobalConfiguration.getConfig("RedisLink"))
      client.getInstance.set("abc", "huanhuan")
  }
}
