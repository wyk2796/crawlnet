package huanhuan.databaseclient.mysql

object MysqlQueryScript {

  def SELECT(tableName:String, columnNames:String): String =
    "select %s from %s".format(columnNames, tableName)

  def SELECT_ALL(tableName:String): String = SELECT("*", tableName)

  def WHERE(tableName:String, columnNames:String, keyColumnName:String, value:String): String =
      "select %s from %s where %s = '%s'".format(columnNames, tableName, keyColumnName, value)

  def IN(tableName:String, columnNames:String, keyColumnName:String, values:String): String =
      "select %s from %s where %s in (%s)".format(columnNames, tableName, keyColumnName, values)

  def UPDATE(tableName:String, setColumns:String, keyColumn:String): String =
      "update %s set %s where %s = {%s}".format(tableName, setColumns, keyColumn, keyColumn)

  def INSERT(tableName:String, columnName:String, values:String): String =
      "insert into %s (%s) value (%s)".format(tableName, columnName, values)

  def DELETE(tableName:String, keyColumnName:String, value:String): String =
    "delete from %s where %s = '%s'".format(tableName, keyColumnName, value)

  def CREATE_TABLE(tableName:String, columnStr:String, primaryKey:String): String =
      "create table %s (%s  primary key (%s)) ENGINE=InnoDB DEFAULT CHARSET=utf8".format(tableName, columnStr, primaryKey)

}


