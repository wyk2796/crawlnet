package crawlnet.core.component.cluster

import crawlnet.GlobalConfiguration
import crawlnet.core.component.AkkaUnit

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