package crawlnet.core.component.cluster

import crawlnet.core.component.AkkaUnit

trait AkkaClusterUnit extends AkkaUnit with GreetOp


trait AkkaClusterMaster extends AkkaUnit
  with RegisterOp with MasterMonitorOp
