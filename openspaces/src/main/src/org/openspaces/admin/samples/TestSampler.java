package org.openspaces.admin.samples;

import com.gigaspaces.grid.gsa.AgentProcessDetails;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.pu.service.ServiceMonitors;

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
//                for (GridServiceManager gsm : admin.getGridServiceManagers()) {
//                    System.out.println("GSM [" + gsm.getUid() + "] running on Machine " + gsm.getMachine().getHostAddress());
//                }
//                for (GridServiceContainer gsc : admin.getGridServiceContainers()) {
//                    System.out.println("GSC [" + gsc.getUid() + "] running on Machine " + gsc.getMachine().getHostAddress());
//                    for (ProcessingUnitInstance puInstance : gsc) {
//                        System.out.println("   -> PU [" + puInstance.getName() + "][" + puInstance.getInstanceId() + "][" + puInstance.getBackupId() + "]");
//                    }
//                }
//                for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
//                    System.out.println("GSA [" + gsa.getUid() + "] running on Machine [" + gsa.getMachine().getHostAddress());
//                    for (AgentProcessDetails processDetails : gsa.getProcessesDetails()) {
//                        System.out.println("   -> Process [" + Arrays.toString(processDetails.getCommand()) + "]");
//                    }
//                }
//                System.out.println("VM TOTAL STATS: Heap Committed [" + admin.getVirtualMachines().getStatistics().getMemoryHeapCommittedInGB() + "GB]");
//                System.out.println("VM TOTAL STATS: GC PERC [" + admin.getVirtualMachines().getStatistics().getGcCollectionPerc() + "], Heap Used [" + admin.getVirtualMachines().getStatistics().getMemoryHeapPerc() + "%]");
//                for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
//                    System.out.println("VM [" + virtualMachine.getUid() + "] " +
//                            "PID [" + virtualMachine.getDetails().getPid() + "] " +
//                            "Host [" + virtualMachine.getMachine().getHostAddress() + "] " +
//                            "GC Perc [" + virtualMachine.getStatistics().getGcCollectionPerc() + "], " +
//                            "Heap Usage [" + virtualMachine.getStatistics().getMemoryHeapPerc() + "%]");
//
//                    for (ProcessingUnitInstance processingUnitInstance : virtualMachine.getProcessingUnitInstances()) {
//                        System.out.println("   -> PU [" + processingUnitInstance.getUid() + "]");
//                    }
//                    for (SpaceInstance spaceInstance : virtualMachine.getSpaceInstances()) {
//                        System.out.println("   -> Space [" + spaceInstance.getUid() + "]");
//                    }
//                }
                for (Machine machine : admin.getMachines()) {
                    System.out.println("Machine [" + machine.getUid() + "], Processors [" + machine.getOperatingSystem().getDetails().getAvailableProcessors() + "] CPU [" + machine.getOperatingSystem().getStatistics().getCpuPerc() + "]");
                    for (OperatingSystemStatistics.NetworkStatistics netStats : machine.getOperatingSystem().getStatistics().getNetworkStats().values()) {
                        System.out.println("   -> " + netStats.getName() + ", Rx " + netStats.getRxBytes() + ", Tx " + netStats.getTxBytes() + ", RxPerSecond " + netStats.getRxBytesPerSecond() + ", TxPerSecond " + netStats.getTxBytesPerSecond());
                    }
//                    System.out.println("   -> Mem Total [" + machine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInGB() + "GB], " + "Free [" + machine.getOperatingSystem().getStatistics().getFreePhysicalMemorySizeInGB() + "GB]");
//                    System.out.println("   -> Swap Total [" + machine.getOperatingSystem().getDetails().getTotalSwapSpaceSizeInGB() + "GB], " + "Free [" + machine.getOperatingSystem().getStatistics().getFreeSwapSpaceSizeInGB() + "GB]");
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
                    for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                        System.out.println("   [" + processingUnitInstance.getClusterInfo() + "] on GSC [" + processingUnitInstance.getGridServiceContainer().getUid() + "]");
                        for (ServiceMonitors monitors : processingUnitInstance.getStatistics()) {
                            System.out.println("      -> Service [" + monitors.getDetails().getId() + "] " + monitors.getMonitors());
                        }
                    }
                }
                for (Space space : admin.getSpaces()) {
                    System.out.println("Space [" + space.getUid() + "] numberOfInstances [" + space.getNumberOfInstances() + "] numberOfbackups [" + space.getNumberOfBackups() + "]");
                    System.out.println("  Stats: Write [" + space.getStatistics().getWriteCount() + "/" + space.getStatistics().getWritePerSecond() + "]");
                    for (SpaceInstance spaceInstance : space) {
                        System.out.println("   -> INSTANCE [" + spaceInstance.getUid() + "] instanceId [" + spaceInstance.getInstanceId() +
                                "] backupId [" + spaceInstance.getBackupId() + "] Mode [" + spaceInstance.getMode() + "]");
                        System.out.println("         -> Host: " + spaceInstance.getMachine().getHostAddress());
                        System.out.println("         -> Stats: Write [" + spaceInstance.getStatistics().getWriteCount() + "/" + spaceInstance.getStatistics().getWritePerSecond() + "]");
                    }
                    for (SpacePartition spacePartition : space.getPartitions()) {
                        System.out.println("   -> Partition [" + spacePartition.getPartitiondId() + "]");
                        for (SpaceInstance spaceInstance : spacePartition) {
                            System.out.println("      -> INSTANCE [" + spaceInstance.getUid() + "]");
                        }
                    }
                }
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
