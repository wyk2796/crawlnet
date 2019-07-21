package huanhuan.databaseclient.Redis

import redis.clients.jedis.Jedis
import com.typesafe.config.Config
import huanhuan.logs.CLog


class RedisClient(basicParam:RedisParam) extends CLog{

  val client: Jedis = createClient()

  def createClient(): Jedis ={
    val client = new Jedis(basicParam.host, basicParam.port, basicParam.timeout)
    basicParam.password match {
      case Some(password) => client.auth(password)
      case None => warn("[REDIS_CLIENT] No password in config!")
    }
    client
  }

  def getInstance: Jedis = client
  def close(): Unit = client.close()
}

object RedisClient{
  import huanhuan.util
  def apply(config:Config): RedisClient = {
    val host = config.getString("host")
    val port = config.getInt("port")
    val timeout = config.getInt("timeout")
    val password = util.configOption(config, "password")
    new RedisClient(RedisParam(password, host, port, timeout))
  }
}

case class RedisParam(password:Option[String] = None, host:String = "127.0.0.1", port:Int = 6379, timeout:Int = 1000)
