package huanhuan.cluster.parameter

trait Parameter {

  private var param:Map[String, Any] = Map.empty[String, Any]

  def addProperty(key:String, value:Any): Unit ={
    param += (key -> value)
  }

  def addProperties[T](iter:Traversable[(String, T)]): Unit ={
    iter.foreach{
      case (key, value) =>
        addProperty(key, value)
    }
  }

  def deleteProperty(key:String):Unit = {
    param -= key
  }

  def get[T](key:String):Option[T] = {
    param.get(key).map(_.asInstanceOf[T])
  }

  def getString(key:String):Option[String] = {
    get[String](key)
  }

  def getInt(key:String):Option[Int] = {
    get[Int](key)
  }

  def getDouble(key:String):Option[Double] = {
    get[Double](key)
  }

  def getBoolean(key:String):Option[Boolean] = {
    get[Boolean](key)
  }

  def getParameters: Map[String, Any] = param

  def merge(other:Parameter): Unit ={
    other.param.foreach{
      case(key, value) =>
        this.param += (key -> value)
    }
  }

  def mergeWithMap(p:Map[String, String]): Unit = {
    p.foreach{
      case(key, value) =>
        this.param += (key -> value)
    }
  }
}
