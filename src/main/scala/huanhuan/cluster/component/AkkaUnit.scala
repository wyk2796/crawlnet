package huanhuan.cluster.component

import akka.actor.Actor
import huanhuan.cluster.parameter.{AkkaParameterOp, Parameter, UnitParameterOp}
import huanhuan.logs.CLog

import scala.util.Random

/** Basic class for every Unit
  * */
private[cluster] trait AkkaUnit extends Actor
  with ComponentUnit with AkkaReceiverManager with CLog{

  val param:AkkaParameter = new AkkaParameter
  val componentHost:AkkaHost = AkkaHost(context.system.settings.config.getString("akka.remote.netty.tcp.hostname"),
    context.system.settings.config.getInt("akka.remote.netty.tcp.port"))
  val akkaAddressBook: AkkaAddressBook = AkkaAddressBook()
  param.setHostAndPort(componentHost.hostname, componentHost.port)
  param.setAkkaAddress(componentHost.toAkkaAddress(self)(context.system))

  def initialUnit(): Unit = {
    param.setComponentName(componentName)
    param.setComponentType(componentType)
    param.setComponentId(componentId)
    param.setReceiverNames(getReceiverNames)
    akkaAddressBook.addAddress(componentId, componentName, componentType, param.getAkkaAddress)
    info(s"[$componentId, $componentName, $componentType]: server is running, address:${param.getAkkaAddress}")
    info(s"[$componentId, $componentName, $componentType]: register receiver:" + getReceiverNames.mkString(","))
  }

  final def sendMessageById(componentId:String, msg:Any): Unit =
    akkaAddressBook
      .getAddressById(componentId)
      .map(context.actorSelection).foreach(_ ! msg)

  final def sendMessageByName(componentName:String, msg:Any):Unit = {
    akkaAddressBook
      .getAddressByName(componentName)
      .map(context.actorSelection).foreach(_ ! msg)
  }

  final def sendMessageByType(componentType:String, msg:Any):Unit = {
    val addresses = akkaAddressBook.getAddressByType(componentType).toArray
    if(!addresses.isEmpty)
      if(addresses.length == 1)
        context.actorSelection(addresses.head) ! msg
      else
        context.actorSelection(addresses(Random.nextInt(addresses.length * 5) % addresses.length)) ! msg
  }

  final def sendMessageByTypeFirst(componentType:String, msg:Any): Unit ={
    akkaAddressBook.getAddressByTypeFirst(componentType).foreach(context.actorSelection(_) ! msg)
  }

  final def sendMessageByTypeBroadcast(componentType:String, msg:Any):Unit = {
    akkaAddressBook.getAddressByType(componentType).foreach{
      address => context.actorSelection(address) ! msg
    }
  }
}

/** Parameters in an component.
  * */
class AkkaParameter
  extends Parameter
    with AkkaParameterOp
    with UnitParameterOp