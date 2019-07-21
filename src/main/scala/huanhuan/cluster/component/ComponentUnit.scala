package huanhuan.cluster.component

import akka.actor.{ActorRef, ActorSystem, Address}
import huanhuan.cluster.parameter.{AkkaParameterOp, Parameter, UnitParameterOp}


/** Basic Component for distribute system
  * */
trait ComponentUnit {
  /**component id */
  val componentId:String
  val componentName:String
  val componentType:String
}

/** Host for Component
  * */

case class AkkaHost(hostname:String, port:Int) extends HostAkkaConvert{
  override def toString:String = s"$hostname:$port"
}

trait HostAkkaConvert{
  self:AkkaHost=>
  def toAkkaAddress(actor: ActorRef)(implicit as:ActorSystem): String = {
    "akka." + actor.path.toStringWithAddress(Address("tcp", as.name, hostname, port))
  }
}