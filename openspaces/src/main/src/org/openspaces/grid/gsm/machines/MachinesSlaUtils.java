package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;

class MachinesSlaUtils {

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

    public static boolean isCapacityRequirementsMet(
            Iterable<GridServiceAgent> agents,
            CapacityRequirements capacityRequirements) {
        
        int machineShortage = capacityRequirements.getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
        long memoryShortageInMB = capacityRequirements.getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
        double cpuShortage = capacityRequirements.getRequirement(CpuCapacityRequirement.class).getCpu();
        
        for (GridServiceAgent agent : agents) {
            machineShortage -= 1;
            memoryShortageInMB -= getMemoryInMB(agent);
            cpuShortage -= getCpu(agent);
        }
        
        return machineShortage<=0 && memoryShortageInMB<=0 && cpuShortage<=0;
    }

    public static int getMemoryInMB(GridServiceAgent agent) {
        return (int) 
            agent.getMachine().getOperatingSystem()
            .getDetails()
            .getTotalPhysicalMemorySizeInMB();
    }

    public static double getCpu(GridServiceAgent agent) {
        return agent.getMachine().getOperatingSystem().getDetails().getAvailableProcessors();
    }


}
