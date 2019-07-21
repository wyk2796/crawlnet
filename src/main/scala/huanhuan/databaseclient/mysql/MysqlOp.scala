package huanhuan.databaseclient.mysql

import com.typesafe.config.Config
import scalikejdbc.SQL

trait MysqlOp{
  self:MysqlClient =>
  import MysqlQueryScript._

  private val columnName = mysqlParam.columns.map(_._1)

  def readAllData(columns:List[String] = List.empty[String]): List[Map[String,Any]] = {
    val columnStr = if(columns.isEmpty) "*" else columns.mkString(",")
    var result:List[Map[String,Any]] = List.empty[Map[String,Any]]
    execution { implicit session =>
      result = SQL(SELECT(mysqlParam.tableName, columnStr)).map(_.toMap()).list().apply()
    }
    result
  }

  def whereGetData(kidValue:String): List[Map[String,Any]] =
    where(mysqlParam.keyColumn,kidValue)

  def where(col:String,value:String): List[Map[String,Any]] = {
    val sqlStr = WHERE(mysqlParam.tableName, "*", col, value)
    var result:List[Map[String,Any]] = List.empty[Map[String,Any]]
    execution { implicit session =>
      result = SQL(sqlStr).map(_.toMap()).list().apply()
    }
    result
  }

  def whereBatch(col:String,columns:List[String] ,value:List[String]):List[Map[String,Any]] = {
    val valueStr = value.map(c =>s"'$c'").mkString(",")
    val colStr = if(columns.isEmpty) "*" else columns.mkString(",")
    val sqlStr = IN(mysqlParam.tableName, colStr, col, valueStr)
    var result:List[Map[String,Any]] = List.empty[Map[String,Any]]
    execution { implicit session =>
      result = SQL(sqlStr).map(_.toMap()).list().apply()
    }
    result
  }


  def read(sqlStr:String):List[Map[String,Any]] = {
    var result:List[Map[String,Any]] = List.empty[Map[String,Any]]
    execution { implicit session =>
      result = SQL(sqlStr).map(_.toMap()).list().apply()
    }
    result
  }

  def update(meta:Record):Int = {
    val columns = columnName
      .filter(_ != mysqlParam.keyColumn)
      .map{c => s"$c = {$c}"}.mkString(",")
    val sqlStr = UPDATE(mysqlParam.tableName, columns, mysqlParam.keyColumn)
    var result:Int = 0
    execution{ implicit session =>
      result = SQL(sqlStr).bindByName(meta.toSymbolArray: _*).update().apply()
    }
    result
  }

  def write(meta:Record): Int = {
    var result:Int = 0
    try{
      val str = columnName.map(x => s"{$x}").mkString(",")
      val sqlStr = INSERT(mysqlParam.tableName, columnName.mkString(","), str)
      execution{ implicit session =>
        result = SQL(sqlStr).bindByName(meta.toSymbolArray: _*).update().apply()
      }
    } catch{
      case e:Exception =>
        println(s"[MYSQL] Insert data have an error, ${e.getMessage}, meta:${meta.toString}")
        result = update(meta)
    }
    result
  }

  def delete(keyValue:String):Boolean = {
    val sqlStr = DELETE(mysqlParam.tableName, mysqlParam.keyColumn, keyValue)
    var result:Boolean = false
    execution{ implicit session =>
      result = SQL(sqlStr).execute().apply()
    }
    result
  }


  def delete(keyValues:List[String]):Unit = {
    keyValues.foreach{
      id =>
        try{
          delete(id)
          info(s"[MYSQL] Delete candidate $id successful")
        }catch{
          case e:Exception =>
            error(s"[MYSQL] Delete candidate $id failed")
            error(e)
        }
    }
  }


  protected def createTable(): Boolean = {
    val columnStr = mysqlParam.columns.map(x => s"${x._1}  ${x._2},").mkString(" ")
    val createTableStr = CREATE_TABLE(mysqlParam.tableName, columnStr, mysqlParam.keyColumn)
    var result:Boolean = false
    execution{ implicit session =>
      result = SQL(createTableStr).execute().apply()
    }
    result
  }
}


trait Record {
  self:Product =>

  def toSymbolArray: Array[(Symbol, Any)] = {
    val clazz = this.getClass
    val columns = clazz.getDeclaredFields.map(x => Symbol(x.getName))
    val values = this.productIterator.toArray
    columns zip values
  }
}
