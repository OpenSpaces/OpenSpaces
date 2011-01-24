package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
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

    public static boolean isManagementRunningOnMachine(Machine machine) {
        return 
            machine.getGridServiceManagers().getSize() > 0 ||
            machine.getLookupServices().getSize() > 0 ||
            machine.getElasticServiceManagers().getSize() > 0;
        
    }
    
    public static long getMemoryInMB(Machine machine, AbstractMachinesSlaPolicy sla) {
        
        final long total = getPhysicalMemoryInMB(machine);
        long reservedMemoryCapacityPerMachineInMB = sla.getReservedMemoryCapacityPerMachineInMB();
        long actual = total-reservedMemoryCapacityPerMachineInMB;
        
        int numberOfContainers = (int) Math.floor(actual / sla.getContainerMemoryCapacityInMB());
        //TODO: Add here maximum number of containers per machine limit
        
        return numberOfContainers * sla.getContainerMemoryCapacityInMB(); 
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
            CapacityRequirements capacityRequirements,
            AbstractMachinesSlaPolicy sla) {
        
        Set<Machine> machines = new HashSet<Machine>();
        for (GridServiceAgent agent : agents) {
            machines.add(agent.getMachine());
        }
        return isCapacityRequirementsMet(machines, capacityRequirements,sla);
    }
    
    private static boolean isCapacityRequirementsMet(
            Iterable<Machine> machines,
            CapacityRequirements capacityRequirements,
            AbstractMachinesSlaPolicy sla) {
        
        int machineShortage = capacityRequirements.getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
        long memoryShortageInMB = capacityRequirements.getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
        double cpuShortage = capacityRequirements.getRequirement(CpuCapacityRequirement.class).getCpu();
        
        for (Machine machine: machines) {
            machineShortage -= 1;
            memoryShortageInMB -= getMemoryInMB(machine, sla);
            cpuShortage -= getCpu(machine);
        }
        
        return machineShortage<=0 && memoryShortageInMB<=0 && cpuShortage<=0;
    }
    
    /**
     * Sort all agents, place management machines first.
     */
    public static List<GridServiceAgent> sortManagementFirst(List<GridServiceAgent> agents) {
        return sortByManagement(agents, true);
    }

    /**
     * Sort all agents, place management machines last.
     */
    public static List<GridServiceAgent> sortManagementLast(List<GridServiceAgent> agents) {
            return sortByManagement(agents, false);
    }
    
    public static List<GridServiceAgent> sortByManagement(List<GridServiceAgent> agents, final boolean managementFirst) {
        List<GridServiceAgent> sortedAgents = new ArrayList<GridServiceAgent>(agents);
        Collections.sort(sortedAgents,new Comparator<GridServiceAgent>() {

            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                boolean management1 = isManagementRunningOnMachine(agent1.getMachine());
                boolean management2 = isManagementRunningOnMachine(agent2.getMachine());
                if (management1 && !management2 && !managementFirst) return  1; // agent2 is smaller since no management
                if (management2 && !management1 && !managementFirst) return -1; // agent1 is smaller since no management
                if (management1 && !management2 && managementFirst)  return -1; // agent1 is smaller since management
                if (management2 && !management1 && managementFirst)  return  1; // agent2 is smaller since management
                return 0;
            }
        });
        
        return sortedAgents;
    }

    public static String machineToString(Machine machine) {
        return machine.getHostName() + "/" + machine.getHostAddress();
    }
    
    public static String gscToString(GridComponent container) {
        return "pid["+container.getVirtualMachine().getDetails().getPid()+"] host["+machineToString(container.getMachine())+"]";
    }

    public static String machinesToString(List<GridServiceAgent> agents) {
        String[] machinesToString = new String[agents.size()];
        for (int i = 0 ; i < machinesToString.length ; i++) {
            machinesToString[i] = machineToString(agents.get(i).getMachine());
        }
        return Arrays.toString(machinesToString);
    }
}
