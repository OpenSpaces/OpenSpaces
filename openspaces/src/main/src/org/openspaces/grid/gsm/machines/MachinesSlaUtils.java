package org.openspaces.grid.gsm.machines;

import java.util.HashSet;
import java.util.Set;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
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
        
        long total = (long) 
            machine.getOperatingSystem().getDetails()
            .getTotalPhysicalMemorySizeInMB(); // 3000
        
        long actual = total-sla.getReservedMemoryCapacityPerMachineInMB(); // 2900
        return actual - (actual % sla.getContainerMemoryCapacityInMB()); // 2900 - (2900 % 500) = 2900 - 400 = 2500
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
}
