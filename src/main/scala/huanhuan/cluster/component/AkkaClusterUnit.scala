package huanhuan.cluster.component

import huanhuan.GlobalConfiguration
import huanhuan.cluster.operation.{MasterRegisterOp, MasterSynchronizationOp, UnitRegisterOp, UnitSynchronizationOp}

trait AkkaClusterUnit extends AkkaUnit
  with UnitSynchronizationOp
  with UnitRegisterOp{

  // add the master information into AddressBook.
  akkaAddressBook.addAddress("default",
    GlobalConfiguration.getString("Master.componentName"),
    GlobalConfiguration.getString("Master.componentType"),
    GlobalConfiguration.getString("Master.akkaAddress"))
}

trait AkkaClusterMaster extends AkkaUnit
  with MasterRegisterOp
  with MasterSynchronizationOp