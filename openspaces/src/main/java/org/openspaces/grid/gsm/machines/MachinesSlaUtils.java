/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.machines;

import com.gigaspaces.grid.gsa.AgentProcessDetails;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.openspaces.admin.Admin;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.core.internal.commons.math.ConvergenceException;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.MachineCapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;

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
    
    /**
     * Sort all agents, place management machines first.
     */
    public static List<GridServiceAgent> sortManagementFirst(Collection<GridServiceAgent> agents) {
        List<GridServiceAgent> sortedAgents = new ArrayList<GridServiceAgent>(agents);
        Collections.sort(sortedAgents,new Comparator<GridServiceAgent>() {
        
            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                boolean management1 = isManagementRunningOnMachine(agent1.getMachine());
                boolean management2 = isManagementRunningOnMachine(agent2.getMachine());
                if (management1 && !management2)  return -1; // agent1 is smaller since management
                if (management2 && !management1)  return  1; // agent2 is smaller since management
                return 0;
            }
        });
        
        return sortedAgents;
    }

    public static String agentToString(Admin admin, String agentUid) {
        GridServiceAgent agent = admin.getGridServiceAgents().getAgentByUID(agentUid);
        if (agent != null) {
            return machineToString(agent.getMachine()) +" "+agent.getExactZones();
        }
        return "agent uid " + agentUid;
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

    
    
    public static CapacityRequirements getMachineTotalCapacity(
            GridServiceAgent agent,
            AbstractMachinesSlaPolicy sla) {

        return new MachineCapacityRequirements(agent.getMachine()).subtractOrZero(getReservedCapacity(sla, agent));
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
        return getContainersByZoneOnMachine(
                    ContainersSlaUtils.getContainerZone(pu), 
                    agent.getMachine())
               .size();
    }

    private static List<GridServiceContainer> getContainersByZoneOnMachine(String zone, Machine machine) {
        List<GridServiceContainer> containers = new ArrayList<GridServiceContainer>();
        for (GridServiceContainer container : machine.getGridServiceContainers()) {
            if (ContainersSlaUtils.isContainerMatchesZone(container,zone)) {
                containers.add(container);
            }
        }
        return containers;
    }
    
    /**
     * Converts the specified agent UUIDs into GridServiceAgent objects.
     * @throws IllegalStateException - if Agent with the specified uid has been removed
     */
    public static Collection<GridServiceAgent> convertAgentUidsToAgents(
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
    
    /**
     * Converts the specified agent UUIDs into GridServiceAgent objects unless these agents are not discovered.
     */
    public static Collection<GridServiceAgent> convertAgentUidsToAgentsIfDiscovered(
            Iterable<String> agentUids, 
            Admin admin) {
        final Collection<GridServiceAgent> agents = new ArrayList<GridServiceAgent>();
        final GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        for (final String agentUid : agentUids) {
            final GridServiceAgent agent = gridServiceAgents.getAgentByUID(agentUid);
            if (agent != null) {
                agents.add(agent);
            }
        }
        return agents;
    }

    /**
     * filters grid service agents by zone, 
     * and if configuration allows management machines, place them first
     * @param agent - the agent to check
     * @param machineProvisioningConfig 
     * @return true if agent meets the sla filter, or false if not (and should be excluded)
     */   
    public static boolean isAgentConformsToMachineProvisioningConfig(GridServiceAgent agent, ElasticMachineProvisioningConfig machineProvisioningConfig) {
        return zoneFilter(agent, machineProvisioningConfig) &&  
               managementFilter(agent, machineProvisioningConfig);
    }

    /**
     * @return true if specified agent matches the machineProvisioningConfig management process (GSM/LUS/ESM) machine isolation criteria.
     */
    private static boolean managementFilter(GridServiceAgent agent, ElasticMachineProvisioningConfig machineProvisioningConfig) {
        return (!machineProvisioningConfig.isDedicatedManagementMachines() ||
         !MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine()));
    }

    /**
     * @return true if specified agent matches the machineProvisioningConfig zone isolation criteria
     */
    private static boolean zoneFilter(GridServiceAgent agent, ElasticMachineProvisioningConfig machineProvisioningConfig) {
        boolean match = agent.getExactZones().isStasfies(machineProvisioningConfig.getGridServiceAgentZones());
        if (agent.getExactZones().getZones().isEmpty() && 
            machineProvisioningConfig.isGridServiceAgentZoneMandatory()) {
            match = false;
        }
        return match;
    }


    public static long getMemoryInMB(CapacityRequirements capacity) {
        return capacity.getRequirement(new MemoryCapacityRequirement().getType()).getMemoryInMB();
    }


    public static boolean isAgentAutoShutdownEnabled(GridServiceAgent agent) {
        String autoShutdownFlag = ContainersSlaUtils.getCommandLineArgumentRemovePrefix(agent, "-Dcom.gs.agent.auto-shutdown-enabled=");
        return Boolean.valueOf(autoShutdownFlag);
    }
    
    public static Collection<GridServiceAgent> sortAndFilterAgents(GridServiceAgent[] agents, ElasticMachineProvisioningConfig machineProvisioningConfig, Log logger) {
        Set<GridServiceAgent> filteredAgents = new LinkedHashSet<GridServiceAgent>(); //maintain order
        for (final GridServiceAgent agent : agents) {
            if (!agent.isDiscovered()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Agent " + MachinesSlaUtils.machineToString(agent.getMachine()) + " has shutdown.");
                }
            }
            else if (!MachinesSlaUtils.isAgentConformsToMachineProvisioningConfig(agent, machineProvisioningConfig)) {
                if (logger.isDebugEnabled()) {
                    agent.getExactZones().isStasfies(machineProvisioningConfig.getGridServiceAgentZones());

                    ExactZonesConfig agentZones = agent.getExactZones();
                    ZonesConfig puZones = machineProvisioningConfig.getGridServiceAgentZones();
                    boolean isDedicatedManagedmentMachines = machineProvisioningConfig.isDedicatedManagementMachines();
                    boolean isManagementRunningOnMachine = MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine());

                    StringBuilder logMessage = new StringBuilder();
                    logMessage.append("Agent ")
                              .append(MachinesSlaUtils.machineToString(agent.getMachine()))
                              .append(" does not conform to machine provisioning SLA. ")
                              .append("Agent zones: ").append(agentZones).append(",")
                              .append("PU zones: ").append(puZones).append(", ")
                              .append("Is dedicated management machines: ").append(isDedicatedManagedmentMachines).append(", ")
                              .append("Is management running on machine: ").append(isManagementRunningOnMachine);

                    logger.debug(logMessage.toString());
                }
            }
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Agent " + MachinesSlaUtils.machineToString(agent.getMachine()) + " conforms to machine provisioning SLA.");
                }
                filteredAgents.add(agent);
            }
        }
        //TODO: Move this sort into the bin packing solver. It already has the priority of each machine
        // so it can sort it by itself.
        final List<GridServiceAgent> sortedFilteredAgents = MachinesSlaUtils.sortManagementFirst(filteredAgents);
        if (logger.isDebugEnabled()) {
            logger.debug("Provisioned Agents: " + MachinesSlaUtils.machinesToString(sortedFilteredAgents));
        }
        return sortedFilteredAgents;
    }


    public static CapacityRequirements getReservedCapacity(
            AbstractMachinesSlaPolicy sla, GridServiceAgent agent) {
        if (isManagementRunningOnMachine(agent.getMachine())) {        	        	
        	CapacityRequirements reservedCapacityPerManagementMachine = sla.getReservedCapacityPerManagementMachine();
			return reservedCapacityPerManagementMachine;
        	
        } else {        	
        	CapacityRequirements reservedCapacityPerMachine = sla.getReservedCapacityPerMachine();
        	return reservedCapacityPerMachine;        	
        }
    	
    }
}
