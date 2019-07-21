package huanhuan.cluster.component

/**
  * Akka address book
  * */

class AkkaAddressBook{
  import AkkaAddressBook._
  private var addresses:Map[String,AddressItem] = Map.empty[String,AddressItem]

  def addressNumber:Long = addresses.size

  def addAddress(componentId:String, componentName:String,
                 componentType:String, address:String): Unit ={
    addresses += (componentId -> AddressItem(componentName, componentType, address))
  }

  def addAddress(componentId:String, address: AddressItem): Unit = {
    addresses += (componentId -> address)
  }

  def getAddressById(componentId:String):Option[String] = {
    addresses.get(componentId).map(_.akkaAddress)
  }

  def getAddressByName(componentName:String):Option[String] = {
    addresses
      .find(_._2.componentName == componentName)
      .map(_._2.akkaAddress)
  }

  def getAddressByTypeFirst(componentType:String):Option[String] = {
    addresses
      .find(_._2.componentType == componentType)
      .map(_._2.akkaAddress)
  }

  def getAddressByTypeRandom(componentType:String): Option[String] = {
    val addList = getAddressByType(componentType).toArray
    if(addList.nonEmpty){
      val index = (math.random()* 100).toInt % addList.length
      Some(addList(index))
    }
    else None

  }

  def getAddressByType(componentType:String):Iterator[String] = {
    addresses
      .filter(_._2.componentType == componentType)
      .map(_._2.akkaAddress).toIterator
  }

  def getAllAddress: Map[String, AddressItem] = addresses

  def removeAddress(label:String):Unit = {
    addresses -= label
  }

  def replace(other:AkkaAddressBook):Unit = addresses = other.getAllAddress

  def merge(other:AkkaAddressBook):Unit = {
    other.getAllAddress.foreach{
      case(label, address) =>
        addAddress(label, address)
    }
  }
}

object AkkaAddressBook{
  def apply(): AkkaAddressBook = new AkkaAddressBook()
  case class AddressItem(componentName:String, componentType:String, akkaAddress:String)
}
