package org.openspaces.grid.gsm.machines;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

public class DefaultMachineProvisioning implements NonBlockingElasticMachineProvisioning {

    private final ProcessingUnit pu;
    private final AbstractMachinesSlaPolicy sla;
    private final Log logger;
    private final MachinesSlaEnforcementState state;
    
    DefaultMachineProvisioning(ProcessingUnit pu, AbstractMachinesSlaPolicy sla, MachinesSlaEnforcementState state) {
        this.state = state;
        this.pu = pu;
        this.sla = sla;
        this.logger = new LogPerProcessingUnit(
                LogFactory.getLog(DefaultMachineProvisioning.class),
                pu);
    }
    
    /**
     * Counts unused grid service agents.
     * If management machines are not allowed they are excluded from the count. 
     */
    public int countFreeAgents() {
        int count = 0;
        Set<String> usedAgents = state.getAllUsedAgentUids();
        for (GridServiceAgent agent : pu.getAdmin().getGridServiceAgents().getAgents()) {
            if (matchesMachineZones(agent) &&
                !usedAgents.contains(agent.getUid()) &&
                (sla.getAllowDeploymentOnManagementMachine() || 
                 !MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine()))) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return a future that when called looks for an empty grid service agent that is not used by any PU.
     * relies on the fact that get() can only be called from a single thread and not from multiple concurrent threads.
     */
    public FutureGridServiceAgents startMachinesAsync(
            final CapacityRequirements capacityRequirements, 
            final long duration, final TimeUnit unit) {
        
        return new FutureGridServiceAgents() {

            final long start = System.currentTimeMillis();
            final long end = start + unit.toMillis(duration);
            
            final Date timestamp = new Date(System.currentTimeMillis());
            
            ExecutionException exception; 
            GridServiceAgent[] allocatedAgents;
            
            public GridServiceAgent[] get() throws ExecutionException, IllegalStateException, TimeoutException {
                
                allocateNow();
                
                if (isTimedOut()) {
                    throw new TimeoutException(
                            "Allocating a new machine took more than "
                                    + unit.toSeconds(duration)
                                    + " seconds to complete.");
                }
                
                if (exception != null) {
                    throw exception;
                }
                
                return allocatedAgents;
            }

            public boolean isDone() {
                
                allocateNow();
                
                return allocatedAgents != null || 
                       exception != null ||
                       isTimedOut();
            }

            public boolean isTimedOut() {
                return System.currentTimeMillis() > end;
            }

            public ExecutionException getException() {
                
                allocateNow();
                
                return exception;
            }

            private void allocateNow() {
                
                if (allocatedAgents != null || exception != null) {
                    return; // idempotent, already allocated
                }
                
                Set<GridServiceAgent> newAgents = new HashSet<GridServiceAgent>();
                Set<String> usedAgentsUids = state.getAllUsedAgentUids();
                
                // add one machine at a time until the sla is met or until there are no more machines left. 
                while (!MachinesSlaUtils.isCapacityRequirementsMet(newAgents, capacityRequirements, sla)) {
                    
                    GridServiceAgent agent = findFreeAgent(usedAgentsUids);
                    if (agent == null) {
                        break;
                    }
                    newAgents.add(agent);
                    usedAgentsUids.add(agent.getUid()); // so we wont allocate this machine again. 
                }
                
                if (exception == null && newAgents.isEmpty()) {
                    StringBuilder usedMachines = new StringBuilder();
                    for (String agentUid : usedAgentsUids) {
                        GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                        if (agent != null) {
                            Machine machine = agent.getMachine();
                            usedMachines.append(machine.getHostAddress()+" has "+ MachinesSlaUtils.getCpu(machine).doubleValue()+ " cpu cores " + MachinesSlaUtils.getPhysicalMemoryInMB(machine)+"MB running " + machine.getGridServiceContainers().getSize() +" containers. ");
                        }
                    }
                    exception = new ExecutionException(
                            new AdminException(
                                    "Out of Machines. "+
                                    "Please start another grid service agent "+
                                    "on a new machine." +
                                    " Used machines: " + usedMachines
                            ));
                }
                
                allocatedAgents = newAgents.toArray(new GridServiceAgent[newAgents.size()]);
            }

            public Date getTimestamp() {
                return timestamp;
            }

            public CapacityRequirements getCapacityRequirements() {
                return capacityRequirements;
            }
        };
    }

    /**
     * remove the specified agent from the started agents list.
     */
    public void stopMachineAsync(GridServiceAgent agent, long duration, TimeUnit unit) {
        // special case where we do not actually kill the agent.
        logger.info(
                "Agent machine " + 
                agent.getMachine().getHostAddress() + " " +
                "has been deallocated. It was not actually shutdown.");
        AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(agent, sla);
        state.markCapacityForDeallocation(pu, agent.getUid(), agentCapacity);
        state.deallocateCapacity(pu, agent.getUid(), agentCapacity);
    }
 
    /**
     * finds a grid service agent that is not in the specified list and not used by GSM/LUS
     * @param usedAgents
     * @param allowDeploymentOnManagementMachine 
     * @return agent if found, or null if no free machines exist.
     */
    private GridServiceAgent findFreeAgent(Set<String> usedAgentUids) {
        List<GridServiceAgent> agents = Arrays.asList(pu.getAdmin().getGridServiceAgents().getAgents());
        
        for (GridServiceAgent agent : MachinesSlaUtils.sortManagementFirst(agents)) {
            
            if (matchesMachineZones(agent) &&
                !usedAgentUids.contains(agent.getUid()) &&
                (sla.getAllowDeploymentOnManagementMachine() || 
                 !MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine()))) {
                return agent;
            }
        }
        return null;
    }
   
    private boolean matchesMachineZones(GridServiceAgent agent) {
        final Set<String> agentZones = new HashSet<String>(agent.getZones().keySet());
        final Set<String> puZones = sla.getDiscoveredMachineZones();
        boolean zoneMatches = agentZones.isEmpty() || puZones.isEmpty();
        agentZones.retainAll(puZones);
        zoneMatches = zoneMatches || !agentZones.isEmpty();
        return zoneMatches;
    }

}
