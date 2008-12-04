package org.openspaces.admin.samples;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

/**
 * @author kimchy
 */
public class TestSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
        while (true) {
            try {
                for (LookupService lookupService : admin.getLookupServices()) {
                    System.out.println("Lookup [" + lookupService.getUid() + "] : " + lookupService.getMachine());
                }
                for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
                    System.out.println("GSM [" + gridServiceManager.getUid() + "] : " + gridServiceManager.getOperatingSystem().getUid());
                }
                for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
                    System.out.println("GSC [" + gridServiceContainer.getUid() + "] : " + gridServiceContainer.getMachine().getUid());
                    for (ProcessingUnitInstance processingUnitInstance : gridServiceContainer) {
                        System.out.println("   -> PU [" + processingUnitInstance.getClusterInfo() + "]");
                    }
                }
                System.out.println("VM TOTAL STATS: Heap Committed [" + admin.getVirtualMachines().getStatistics().getMemoryHeapCommitted() +"]");
                for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
                    System.out.println("VM [" + virtualMachine.getUid() + "] on host [" + virtualMachine.getMachine().getHost() + "]");
                    for (ProcessingUnitInstance processingUnitInstance : virtualMachine.getProcessingUnitInstances()) {
                        System.out.println("   -> PU [" + processingUnitInstance.getUid() + "]");
                    }
                    for (SpaceInstance spaceInstance : virtualMachine.getSpaceInstances()) {
                        System.out.println("   -> Space [" + spaceInstance.getUid() + "]");
                    }
                }
                for (Machine machine : admin.getMachines()) {
                    System.out.println("Machine [" + machine.getUid() + "], transports: " + machine.getOperatingSystem().getUid());
                    for (SpaceInstance spaceInstance : machine.getSpaceInstances()) {
                        System.out.println("   -> Space [" + spaceInstance.getUid() + "]");
                    }
                    for (ProcessingUnitInstance processingUnitInstance : machine.getProcessingUnitInstances()) {
                        System.out.println("   -> PU [" + processingUnitInstance.getUid() + "]");
                    }
                }
                for (ProcessingUnit processingUnit : admin.getProcessingUnits()) {
                    System.out.println("Processing Unit: " + processingUnit.getName() + " status: " + processingUnit.getStatus());
                    if (processingUnit.isManaged()) {
                        System.out.println("   -> Managing GSM: " + processingUnit.getManagingGridServiceManager().getUid());
                    } else {
                        System.out.println("   -> Managing GSM: NA");
                    }
                    for (GridServiceManager backupGSM : processingUnit.getBackupGridServiceManagers()) {
                        System.out.println("   -> Backup GSM: " + backupGSM.getUid());
                    }
                    for (ProcessingUnitPartition partition : processingUnit.getPartitions()) {
                        System.out.println("   : Partition [" + partition.getPartitiondId() + "] Instances [" + partition.getInstances().length + "]");
                    }
                    for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                        System.out.println("   [" + processingUnitInstance.getClusterInfo() + "] on GSC [" + processingUnitInstance.getGridServiceContainer().getUid() + "] partition [" + processingUnitInstance.getPartition().getPartitiondId() + "]");
                        if (processingUnitInstance.isEmbeddedSpaces()) {
                            System.out.println("      -> Embedded Space [" + processingUnitInstance.getSpaceInstance().getUid() + "]");
                        }
                        for (ProcessingUnitServiceDetails details : processingUnitInstance) {
                            System.out.println("      -> Service [" + details.getId() + "] type [" + details.getType() + "]");
                        }
                    }
                }
                for (Space space : admin.getSpaces()) {
                    System.out.println("Space [" + space.getUid() + "] numberOfInstances [" + space.getNumberOfInstances() + "] numberOfbackups [" + space.getNumberOfBackups() + "]");
                    for (SpaceInstance spaceInstance : space) {
                        System.out.println("   -> INSTANCE [" + spaceInstance.getUid() + "] instanceId [" + spaceInstance.getInstanceId() +
                                "] backupId [" + spaceInstance.getBackupId() + "] Partiton [" + spaceInstance.getPartition().getPartitiondId() + "]");
                        System.out.println("         -> Host: " + spaceInstance.getMachine().getHost());
                        SpaceInstanceStatistics statistics = spaceInstance.getStatistics();
                        System.out.println("         -> Stats: Write [" + statistics.getWriteCount() + "], Read [" + statistics.getReadCount() + "]");
                    }
                    for (SpacePartition spacePartition : space.getPartitions()) {
                        System.out.println("   -> Partition [" + spacePartition.getPartitiondId() + "]");
                        for (SpaceInstance spaceInstance : spacePartition) {
                            System.out.println("      -> INSTANCE [" + spaceInstance.getUid() + "]");
                        }
                    }
                }
                System.out.println("*********************************************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
