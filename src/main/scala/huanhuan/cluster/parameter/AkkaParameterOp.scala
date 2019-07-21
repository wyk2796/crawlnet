package huanhuan.cluster.parameter

trait AkkaParameterOp{
  self:Parameter =>

  def setAkkaAddress(address:String): Unit ={
    addProperty("Akka_Address", address)
  }

  def getAkkaAddress: String = {
    get[String]("Akka_Address").get
  }

  def setHostAndPort(host:String, port:Int): Unit ={
    addProperty("Host", host)
    addProperty("Port", port)
  }

  def getHostAndPort: (Option[String], Option[Int]) ={
    get[String]("Host") -> getInt("Port")
  }

  def setReceiverNames(names:Iterable[String]): Unit ={
    addProperty("ReceiverNames", names.mkString(","))
  }

  def getReceiverNames:Option[Iterable[String]] = {
    get[String]("ReceiverNames").map(_.split(",").toIterable)
  }

}

