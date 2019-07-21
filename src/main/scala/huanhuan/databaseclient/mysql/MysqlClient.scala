package huanhuan.databaseclient.mysql

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import huanhuan.databaseclient.mysql.MysqlClient.MYSQLParam
import huanhuan.logs.CLog
import scalikejdbc.{ConnectionPool, DB, DBSession, DataSourceConnectionPool, using}
import scala.collection.convert.ImplicitConversionsToScala._

abstract class MysqlClient(p:MYSQLParam) extends CLog{
  val mysqlParam: MYSQLParam = p

  Class.forName("com.mysql.jdbc.Driver")

  private val hConfig = new HikariConfig()
  hConfig.setJdbcUrl(s"${mysqlParam.url}/${mysqlParam.dbName}")
  hConfig.setUsername(mysqlParam.useName)
  hConfig.setPassword(mysqlParam.passWord)
  val hDataSource = new HikariDataSource(hConfig)
  ConnectionPool.add(mysqlParam.dbName,new DataSourceConnectionPool(hDataSource))

  //check the database and table.
  using(DB(ConnectionPool.borrow(mysqlParam.dbName))){
    db =>
      if(db.getTableNames().contains(mysqlParam.tableName))
        info(s"connect table $mysqlParam.tableName successful")
      else{
        createTable()
        info(s"create table $mysqlParam.tableName successful")
      }
  }


  protected def execution(fun: DBSession => Unit): Unit ={
    using(DB(ConnectionPool.borrow(p.dbName))) {
      db =>
        db.autoCommit{
          session =>
            fun(session)
        }
    }
  }

  protected def createTable():Boolean
}


object MysqlClient{
  case class MYSQLParam(url:String,dbName:String,useName:String,passWord:String,tableName:String,keyColumn:String,columns:Array[(String,String)]){
    override def toString:String = s"$url:$dbName:$useName:$passWord:$tableName:${columns.mkString(";")}"
  }
}


class MySqlInstance(p:MYSQLParam) extends MysqlClient(p) with MysqlOp

object MySqlInstance{

  def apply(config:Config):Map[String,MySqlInstance] = {
    val connects = config.getConfig("mysql").getConfigList("connect")
    val dataParams = for(t <- connects) yield {
      val url = t.getString("url")
      val dbName = t.getString("dbName")
      val useName = t.getString("useName")
      val passWord = t.getString("passWord")
      val table = t.getConfig("table")
      val tableName:String = table.getString("table_name")
      val key = table.getString("key_column")
      val columns = table
        .getConfigList("columns")
        .map{
          f =>
            f.getString("name") -> f.getString("type")
        }.toArray
      MYSQLParam(url,dbName,useName,passWord,tableName,key,columns)
    }
    dataParams.toArray.map(db => db.tableName -> new MySqlInstance(db)).toMap
  }
}