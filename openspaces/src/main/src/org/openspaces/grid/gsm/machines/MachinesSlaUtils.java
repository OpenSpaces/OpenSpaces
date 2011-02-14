package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openspaces.admin.Admin;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.core.internal.commons.math.ConvergenceException;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

import com.gigaspaces.grid.gsa.AgentProcessDetails;

public class MachinesSlaUtils {

    public static Set<Long> getChildProcessesIds(final GridServiceAgent agent) {
        Set<Long> pids = new HashSet<Long>();
        for (AgentProcessDetails details : agent.getProcessesDetails().getProcessDetails()) {
            pids.add(details.getProcessId());
        }
        return pids;
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
    
    public static Fraction getCpu(Machine machine) {
        int availableProcessors = machine.getOperatingSystem().getDetails().getAvailableProcessors();
        return new Fraction(availableProcessors,1);
        
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
            cpuShortage -= getCpu(machine).doubleValue();
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
    public static List<GridServiceAgent> sortManagementLast(Collection<GridServiceAgent> agents) {
            return sortByManagement(agents, false);
    }
    
    public static List<GridServiceAgent> sortByManagement(Collection<GridServiceAgent> agents, final boolean managementFirst) {
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

    public static String machinesToString(Collection<GridServiceAgent> agents) {
        String[] machinesToString = new String[agents.size()];
        int i =0;
        for (GridServiceAgent agent: agents) {
            machinesToString[i] = machineToString(agent.getMachine());
            i++;
        }
        return Arrays.toString(machinesToString);
    }
    

    
    public static boolean matchesMachineZones(AbstractMachinesSlaPolicy sla, GridServiceAgent agent) {
        final Set<String> agentZones = new HashSet<String>(agent.getZones().keySet());
        final Set<String> puZones = sla.getMachineZones();
        boolean zoneMatches = agentZones.isEmpty() || puZones.isEmpty();
        agentZones.retainAll(puZones);
        zoneMatches = zoneMatches || !agentZones.isEmpty();
        return zoneMatches;
    }

    public static AllocatedCapacity convertCapacityRequirementsToAllocatedCapacity(
            CapacityRequirements capacityRequirements) {
        
        Fraction cpuCores = convertCpuCoresFromDoubleToFraction(
                capacityRequirements.getRequirement(CpuCapacityRequirement.class).getCpu());
        long memoryInMB = capacityRequirements.getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
        return new AllocatedCapacity(cpuCores,memoryInMB);
    }

    public static AllocatedCapacity getMaxAllocatedCapacity(
            GridServiceAgent agent,
            AbstractMachinesSlaPolicy sla) {

            return new AllocatedCapacity(
                    getCpu(agent.getMachine()),
                    getMemoryInMB(agent.getMachine(), sla));
    }

    public static Fraction convertCpuCoresFromDoubleToFraction(double cpu) {
        Fraction targetCpuCores;
        try {
            targetCpuCores = new Fraction(cpu);
        } catch (ConvergenceException e) {
            targetCpuCores = new Fraction((int)Math.ceil(cpu*2),2);
        }
        return targetCpuCores;
    }

    public static int getNumberOfChildContainersForProcessingUnit(GridServiceAgent agent, ProcessingUnit pu) {
        // the reason we are looking at the machine and not on the agent is that a container 
        // could be orphan for a short period (meaning it was discovered before the agent that started it).
        return ContainersSlaUtils.getContainersByZoneOnMachine(
                    ContainersSlaUtils.getContainerZone(pu), 
                    agent.getMachine())
               .size();
    }

    /**
     * Converts the specified agent UUIDs into GridServiceAgent objects.
     * @throws IllegalStateException - if Agent with the specified uid has been removed
     */
    public static Collection<GridServiceAgent> getGridServiceAgentsFromUids(
            Iterable<String> agentUids, 
            Admin admin) {
        final Collection<GridServiceAgent> agents = new ArrayList<GridServiceAgent>();
        final GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        for (final String agentUid : agentUids) {
            final GridServiceAgent agent = gridServiceAgents.getAgentByUID(agentUid);
            if (agent == null) {
                throw new IllegalStateException("At this point agent " + agentUid + " must be discovered.");
            }
            agents.add(agent);
        }
        return agents;
    }
}
