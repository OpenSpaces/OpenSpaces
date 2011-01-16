package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.machine.Machine;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;

public class MachinesSlaUtils {

    public static int getNumberOfChildProcesses(final GridServiceAgent agent) {
        int numberOfChildProcesses = agent.getProcessesDetails().getProcessDetails().length;
        return numberOfChildProcesses;
    }

    public static boolean isManagementRunningOnGridServiceAgent(GridServiceAgent agent) {
        return getNumberOfChildProcesses(agent) - getNumberOfChildContainers(agent) > 0;
    }
    
    private static int getNumberOfChildContainers(final GridServiceAgent agent) {
        int numberOfContainers = 0;
        for (final GridServiceContainer container : agent.getAdmin().getGridServiceContainers()) {
            if (container.getGridServiceAgent() != null && container.getGridServiceAgent().equals(agent)) {
                numberOfContainers++;
            }
        }
        return numberOfContainers;
    }

    

    public static long getMemoryInMB(Machine machine, MachinesSlaPolicy sla) {
        
        final long total = getPhysicalMemoryInMB(machine);
        
        long reservedMemoryCapacityPerMachineInMB = sla.getReservedMemoryCapacityPerMachineInMB();
        
        long reservedMemoryCapacityByManagersInMB = getUsedMemoryCapacityByManagersInMB(machine);
        
        long actual = total-reservedMemoryCapacityPerMachineInMB-reservedMemoryCapacityByManagersInMB;
        
        return actual - (actual % sla.getContainerMemoryCapacityInMB()); 
    }

    private static long getUsedMemoryCapacityByManagersInMB(Machine machine) {
        long reservedMemoryCapacityByManagersInMB = 0;
        for (final GridServiceManager gsm : machine.getGridServiceManagers()) {
            reservedMemoryCapacityByManagersInMB += getJvmHeapMemoryInMB(gsm);
        }
        
        for (final ElasticServiceManager esm : machine.getElasticServiceManagers()) {
            reservedMemoryCapacityByManagersInMB += getJvmHeapMemoryInMB(esm);
        }
        
        for (final LookupService lus : machine.getLookupServices()) {
            reservedMemoryCapacityByManagersInMB += getJvmHeapMemoryInMB(lus);
        }
        return reservedMemoryCapacityByManagersInMB;
    }
    
    private static long getJvmHeapMemoryInMB(GridComponent gridComponent) {
        long memoryHeapInitInBytes = gridComponent.getVirtualMachine().getDetails().getMemoryHeapInitInBytes();
        long memoryHeapMaxInBytes = gridComponent.getVirtualMachine().getDetails().getMemoryHeapMaxInBytes();
        return MemoryUnit.MEGABYTES.convert(Math.max(memoryHeapInitInBytes, memoryHeapMaxInBytes), MemoryUnit.BYTES);
        
    }

    public static long getPhysicalMemoryInMB(Machine machine) {
        final long total = (long) 
            machine.getOperatingSystem().getDetails()
            .getTotalPhysicalMemorySizeInMB();
        return total;
    }
    
    public static double getCpu(Machine machine) {
        return machine.getOperatingSystem().getDetails().getAvailableProcessors();
    }

    public static boolean isCapacityRequirementsMet(
            Set<GridServiceAgent> agents,
            CapacityRequirements capacityRequirements) {
        
        Set<Machine> machines = new HashSet<Machine>();
        for (GridServiceAgent agent : agents) {
            machines.add(agent.getMachine());
        }
        return isCapacityRequirementsMet(machines, capacityRequirements);
    }
    
    private static boolean isCapacityRequirementsMet(
            Iterable<Machine> machines,
            CapacityRequirements capacityRequirements) {
        
        int machineShortage = capacityRequirements.getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
        long memoryShortageInMB = capacityRequirements.getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
        double cpuShortage = capacityRequirements.getRequirement(CpuCapacityRequirement.class).getCpu();
        
        for (Machine machine: machines) {
            machineShortage -= 1;
            memoryShortageInMB -= getMemoryInMB(machine);
            cpuShortage -= getCpu(machine);
        }
        
        return machineShortage<=0 && memoryShortageInMB<=0 && cpuShortage<=0;
    }

    private static int getMemoryInMB(Machine machine) {
        return (int) 
            machine.getOperatingSystem()
            .getDetails()
            .getTotalPhysicalMemorySizeInMB();
    }

    public static GridServiceAgent[] sortManagementLast(GridServiceAgent[] agents) {
        List<GridServiceAgent> sortedAgents = new ArrayList<GridServiceAgent>(Arrays.asList(agents));
        Collections.sort(sortedAgents,new Comparator<GridServiceAgent>() {

            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                boolean management1 = isManagementRunningOnGridServiceAgent(agent1);
                boolean management2 = isManagementRunningOnGridServiceAgent(agent2);
                if (management1 && !management2) return 1; // agent2 is smaller since no management
                if (management2 && !management1) return -1;// agent1 is smaller since no management
                return 0;
            }
        });
        
        return sortedAgents.toArray(new GridServiceAgent[sortedAgents.size()]);
    }
}
