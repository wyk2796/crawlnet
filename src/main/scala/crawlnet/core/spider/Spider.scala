package crawlnet.core.spider

import akka.actor.{Actor, ActorRef, Props}
import crawlnet.core.component.Parameter
import crawlnet.core.component.cluster.AkkaClusterUnit
import crawlnet.core.logs.CLog
import crawlnet.linkserver.LinkMessage.{NewLinks, NextLinks, RequestLink}

import scala.collection.mutable

trait SpiderServer extends AkkaClusterUnit with SpiderWorkSpace


//trait LinkManage{
//  self:AkkaClusterUnit =>

//  private val linkQueue: mutable.Queue[String] = mutable.Queue.empty[String]
//  private var queueLength:Int = 300

//  receiveAdd("LinkManage", linkReceive)
//  def isEmpty:Boolean = linkQueue.isEmpty
//  def setLength(length:Int):Unit = queueLength = length

//  def requestLink(num:Int): Unit = {
//    sendMessageByType("LinkServer", RequestLink(num, param.getAkkaAddress))
//  }

//  def nextLink:String = {
//    val links = linkQueue.dequeue()
//    if(isEmpty) requestLink(queueLength)
//    links
//  }

//  def saveCollectedLink(links:TraversableOnce[String]): Unit = {
//    sendMessageByType("LinkServer", NewLinks(linkFilter(links)))
//  }



//  def linkFilter(links:TraversableOnce[String]):TraversableOnce[String] = links
//
//  def linkReceive:Receive = {
//    case NextLinks(links) =>
//      links.foreach(linkQueue.enqueue(_))
//  }
//}


trait Worker extends Actor with CLog{
  import Worker._
  val workParam:Parameter = new Parameter{}

  def initial(param:Map[String,Any]): Unit = {
    param.foreach{
      case(key, value) =>
        workParam.addProperty(key, value)
    }
  }
  def runTask(task:Any)
  def taskDone(): Unit ={
    context.parent ! TaskComplete()
  }

  def stop(): Unit = context stop self

  override def receive: Receive = {
    case Configuration(params) =>
      initial(params)
    case WorkTask(task) =>
      runTask(task)
      taskDone()
    case Idle() =>
      Thread.sleep(3000)
      taskDone()
    case WorkStop() =>
      stop()
  }
}

object Worker{
  trait WMessage
  case class Configuration(params:Map[String,Any]) extends WMessage
  case class WorkTask(task:Any) extends WMessage
  case class Idle() extends WMessage
  case class WorkStop() extends WMessage
  case class TaskComplete() extends WMessage
}

trait SpiderWorkSpace{
  self:AkkaClusterUnit =>
  import Worker._

  private val MAX_WORK_NUM = 100
//  private val MAX_TASK_NUM = 10000
  private var count:Int = 0
  private var workPool: Array[ActorRef] = Array.empty[ActorRef]
  private val taskPool: mutable.Queue[WorkTask] = mutable.Queue.empty[WorkTask]
  def workLen: Int = workPool.length
  def taskLen: Int = taskPool.length

  receiveAdd("WorkSpace", WorkerReceive)
  def createWorkWithClasspath(num:Int, classPath:String): Unit = {
    val workNum = math.min(num, MAX_WORK_NUM)
    for(i <- 0 until workNum) {
      val workName = s"${componentName}_$i"
      val ref = try{
        context.actorOf(Props(Class.forName(classPath)), workName)
      } catch{
        case e:Exception =>
          error(s"[$componentId, $componentName]: Worker Creating: Wrong $classPath.")
          error(e.fillInStackTrace())
          throw new Exception("Creating Work Failed!")
      }
      workPool = workPool :+ ref
      info(s"[$componentId, $componentName]: worker $workName is generated.")
    }
  }

  def requestLink(num:Int): Unit = {
    sendMessageByType("LinkServer", RequestLink(num, param.getAkkaAddress))
  }


  def initialTasks(tasks:TraversableOnce[WorkTask]):Unit =
    tasks.foreach(taskPool.enqueue(_))


  def broadcast(msg:WMessage): Unit = {
    workPool.foreach(_ ! msg)
  }

  def roundRobin(msg:WMessage): Unit = {
    workPool(count % workLen) ! msg
    count += 1
    if(count >= 200) count = 0
  }

  def assignTask(): Unit ={
    if(taskPool.nonEmpty)
      roundRobin(taskPool.dequeue())
    else{
      roundRobin(Idle())
    }
  }

  def initialWorker(params:Map[String,Any]): Unit = {
    broadcast(Configuration(params))
  }

  def stopWorker(): Unit ={
    broadcast(WorkStop())
  }

  def createTask(p:String): WorkTask


  def WorkerReceive:Receive = {
    case TaskComplete() =>
      assignTask()
      info(s"[$componentId,$componentName]: the rest of task is ${taskLen}")
      if(taskLen < workLen) requestLink(workLen * 10)
    case NextLinks(links) =>
      links.foreach{
        link =>
          taskPool.enqueue(createTask(link))
      }
  }

}