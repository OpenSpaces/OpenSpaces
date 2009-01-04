package org.openspaces.admin.samples;

import com.gigaspaces.grid.gsa.AgentProcessDetails;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.pu.service.ServiceDetails;

import java.util.Arrays;

/**
 * @author kimchy
 */
public class TestSampler {

    public static void main(String[] args) throws InterruptedException {
        Admin admin = new AdminFactory().addGroup("kimchy").createAdmin();
//        admin.getGridServiceManagers().waitFor(2);
//        Space space1 = admin.getSpaces().waitFor("test");
//        space1.waitFor(1, SpaceMode.PRIMARY);
        while (true) {
            try {
//                for (LookupService lookupService : admin.getLookupServices()) {
//                    System.out.println("Lookup [" + lookupService.getUid() + "] : " + lookupService.getMachine());
//                }
//                for (GridServiceManager gridServiceManager : admin.getGridServiceManagers()) {
//                    System.out.println("GSM [" + gridServiceManager.getUid() + "] : " + gridServiceManager.getOperatingSystem().getUid());
//                }
//                for (GridServiceContainer gridServiceContainer : admin.getGridServiceContainers()) {
//                    System.out.println("GSC [" + gridServiceContainer.getUid() + "] : " + gridServiceContainer.getMachine().getUid());
//                    for (ProcessingUnitInstance processingUnitInstance : gridServiceContainer) {
//                        System.out.println("   -> PU [" + processingUnitInstance.getClusterInfo() + "]");
//                    }
//                }
                for (GridServiceAgent gridServiceAgent : admin.getGridServiceAgents()) {
                    System.out.println("GSA [" + gridServiceAgent.getUid() + "]");
                    for (AgentProcessDetails processDetails : gridServiceAgent.getProcessesDetails().getProcessDetails()) {
                        System.out.println("   -> Process [" + Arrays.toString(processDetails.getCommand()) + "]");
                    }
                }
//                System.out.println("VM TOTAL STATS: Heap Committed [" + admin.getVirtualMachines().getStatistics().getMemoryHeapCommittedInBytes() +"]");
//                System.out.println("VM TOTAL STATS: GC PERC [" + admin.getVirtualMachines().getStatistics().getGcCollectionPerc() + "], Heap Used [" + admin.getVirtualMachines().getStatistics().getMemoryHeapPerc() + "%]");
//                for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
//                    System.out.println("VM [" + virtualMachine.getUid() + "] on host [" + virtualMachine.getMachine().getHost() + "] GC Perc [" + virtualMachine.getStatistics().getGcCollectionPerc() + "], Head Usage [" + virtualMachine.getStatistics().getMemoryHeapPerc() + "%]");
//                    for (ProcessingUnitInstance processingUnitInstance : virtualMachine.getProcessingUnitInstances()) {
//                        System.out.println("   -> PU [" + processingUnitInstance.getUid() + "]");
//                    }
//                    for (SpaceInstance spaceInstance : virtualMachine.getSpaceInstances()) {
//                        System.out.println("   -> Space [" + spaceInstance.getUid() + "]");
//                    }
//                }
                for (Machine machine : admin.getMachines()) {
                    System.out.println("Machine [" + machine.getUid() + "], TotalPhysicalMem [" + machine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInGB() + "GB], " +
                            "FreePhysicalMem [" + machine.getOperatingSystem().getStatistics().getFreePhysicalMemorySizeInGB() + "GB], " +
                            "SystemLoadAvg [" + machine.getOperatingSystem().getStatistics().getSystemLoadAverage() + "]");
//                    for (SpaceInstance spaceInstance : machine.getSpaceInstances()) {
//                        System.out.println("   -> Space [" + spaceInstance.getUid() + "]");
//                    }
//                    for (ProcessingUnitInstance processingUnitInstance : machine.getProcessingUnitInstances()) {
//                        System.out.println("   -> PU [" + processingUnitInstance.getUid() + "]");
//                    }
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
                        for (ServiceDetails details : processingUnitInstance) {
                            System.out.println("      -> Service " + details);
                        }
                    }
                }
//                for (Space space : admin.getSpaces()) {
//                    System.out.println("Space [" + space.getUid() + "] numberOfInstances [" + space.getNumberOfInstances() + "] numberOfbackups [" + space.getNumberOfBackups() + "]");
//                    System.out.println("  Stats: Write [" + space.getStatistics().getWriteCount() + "/" + space.getStatistics().getWritePerSecond() + "], Take [" + space.getStatistics().getTakeCount() + "/" + space.getStatistics().getTakePerSecond() + "]");
//                    for (SpaceInstance spaceInstance : space) {
//                        System.out.println("   -> INSTANCE [" + spaceInstance.getUid() + "] instanceId [" + spaceInstance.getInstanceId() +
//                                "] backupId [" + spaceInstance.getBackupId() + "] Partiton [" + spaceInstance.getPartition().getPartitiondId() + "]");
//                        System.out.println("         -> Host: " + spaceInstance.getMachine().getHost());
//                        System.out.println("         -> Stats: Write [" + spaceInstance.getStatistics().getWriteCount() + "/" + spaceInstance.getStatistics().getWritePerSecond() + "], Take [" + spaceInstance.getStatistics().getTakeCount() + "/" + spaceInstance.getStatistics().getTakePerSecond() + "]");
//                    }
//                    for (SpacePartition spacePartition : space.getPartitions()) {
//                        System.out.println("   -> Partition [" + spacePartition.getPartitiondId() + "]");
//                        for (SpaceInstance spaceInstance : spacePartition) {
//                            System.out.println("      -> INSTANCE [" + spaceInstance.getUid() + "]");
//                        }
//                    }
//                }
//                System.out.println("Transport TOTAL: ActiveThreads [" + admin.getTransports().getStatistics().getActiveThreadsCount() + "], CompletedTaskPerSecond [" + admin.getTransports().getStatistics().getCompletedTaskPerSecond() + "]");
//                for (Transport transport : admin.getTransports()) {
//                    System.out.println("Transport [" + transport.getUid() + "] ActiveThreads [" + transport.getStatistics().getActiveThreadsCount() + "], CompletedTaskPerSecond [" + transport.getStatistics().getCompletedTaskPerSecond() + "]");
//                }
                System.out.println("*********************************************************************");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }
    }
}
