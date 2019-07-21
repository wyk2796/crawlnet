package huanhuan.webcollect.spider

import akka.actor.{ActorContext, ActorRef, Props}
import huanhuan.cluster.component.AkkaClusterUnit
import huanhuan.webcollect.linkserver.LinkMessage.{NextLinks, RequestLink}
import huanhuan.webcollect.spider.Worker.WorkTask
import scala.collection.mutable


trait WorkSpace[T]{
  self:AkkaClusterUnit =>
  type Task = T
  var MAX_WORK_NUM: Int = 100
  var workPool: Array[ActorRef] = Array.empty[ActorRef]
  val taskPool: mutable.Queue[Task] = mutable.Queue.empty[Task]
  def workLen: Int = workPool.length
  def taskLen: Int = taskPool.length
  receiveAdd("WorkSpace", workerSpaceReceive)

  def setMaxWorkerNum(max:Int): Unit
  def insertTask(task:Task):Unit
  def insertTasks(tasks:TraversableOnce[Task]):Unit

  def createWorkers(workerNum:Int, props:Props):Unit
  def initialWorker(params:Map[String,Any]):Unit
  def beginWork():Unit
  def stopWork():Unit
  def idleWork():Unit

  def assignTask(workerRef: ActorRef):Unit
  def generateTask(args:Any):Task

  def workerSpaceReceive:Receive
}


trait SpiderWorkSpace extends WorkSpace[WorkTask] {
  self:AkkaClusterUnit =>
  import Worker._
  private var count:Int = 0

  //test param
  var timeRecord1:Long = 0

  override def setMaxWorkerNum(max: Int): Unit = MAX_WORK_NUM = max
  def insertTask(task: WorkTask): Unit = taskPool.enqueue(task)
  def insertTasks(tasks: TraversableOnce[WorkTask]): Unit = tasks.foreach(insertTask)

  def createWorkers(num:Int, props: Props): Unit = {
    for(i <- 0 until math.min(num, MAX_WORK_NUM)) {
      val workName = s"${componentName}_$i"
      val ref = try{
        context.actorOf(props, workName)
      } catch{
        case e:Exception =>
          error(s"[$componentId, $componentName]: Worker Creating: Wrong.")
          error(e.fillInStackTrace())
          throw new Exception("Creating Work Failed!")
      }
      workPool = workPool :+ ref
      info(s"[$componentId, $componentName]: worker $workName is generated.")
    }
  }

  def beginWork(): Unit ={
    broadcast(WorkStart)
  }

  def idleWork(): Unit ={
    broadcast(Idle(5000))
  }

  def broadcast(msg:WMessage): Unit = {
    workPool.foreach(_ ! msg)
  }


  def assignTask(workerRef:ActorRef): Unit ={
    if(taskPool.nonEmpty){
      workerRef ! taskPool.dequeue()
      count += 1
    } else{
      workerRef ! Idle(300)
    }
  }

  def initialWorker(params:Map[String,Any]): Unit = {
    broadcast(Configuration(params))
  }

  def stopWork(): Unit ={
    broadcast(WorkStop)
  }

  def workerSpaceReceive:Receive = {
    case RequestTask(ref) =>
      assignTask(ref)
      info(s"[$componentId,$componentName]: the rest of task is $taskLen")
      if(taskLen % 10 == 0){
        val intervalTime = System.currentTimeMillis() - timeRecord1
        info(s"[#########Text] 10 task cost ${intervalTime}s, per ${intervalTime / 10}")
        timeRecord1 = System.currentTimeMillis()
      }
      if(taskLen < workLen)
        sendMessageByType("LinkServer",
          RequestLink(workLen * 10, param.getAkkaAddress))
    case NextLinks(links) =>
      links.map(generateTask).foreach(insertTask)
  }

}