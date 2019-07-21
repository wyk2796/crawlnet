package huanhuan.cluster.parameter

trait UnitParameterOp{
  self:Parameter =>

  def setComponentId(id:String):Unit = {
    addProperty("Component_ID", id)
  }

  def setComponentName(name:String):Unit = {
    addProperty("Component_Name", name)
  }

  def setComponentType(componentType:String):Unit = {
    addProperty("Component_Type", componentType)
  }

  def getComponentId:String = {
    get[String]("Component_ID").get
  }

  def getComponentName:String = {
    get[String]("Component_Name").get
  }

  def getComponentType:String = {
    get[String]("Component_Type").get
  }
}