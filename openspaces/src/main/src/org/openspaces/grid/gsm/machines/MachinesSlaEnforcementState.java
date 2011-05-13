package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.capacity.MachineCapacityRequirements;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;

public class MachinesSlaEnforcementState {
    
    private final Log logger;
    
    // state that tracks managed grid service agents, agents to be started and agents marked for shutdown.
    private final Map<ProcessingUnit,ClusterCapacityRequirements> allocatedCapacityPerProcessingUnit;
    private final Map<ProcessingUnit,List<GridServiceAgentFutures>> futureAgentsPerProcessingUnit;
    private final Map<ProcessingUnit,ClusterCapacityRequirements> markedForDeallocationCapacityPerProcessingUnit;
    private final Map<ProcessingUnit, ElasticProcessingUnitMachineIsolation> machineIsolationPerProcessingUnit;
    private final Map<ProcessingUnit,Map<String,Long>> timeoutTimestampPerAgentUidGoingDownPerProcessingUnit;
    private final Set<GridServiceAgent> agentsStartedByMachineProvisioning;
    
    public MachinesSlaEnforcementState() {
        this.logger = 
                new SingleThreadedPollingLog( 
                        LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class));
                        
        allocatedCapacityPerProcessingUnit = new HashMap<ProcessingUnit, ClusterCapacityRequirements>();
        futureAgentsPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceAgentFutures>>();
        markedForDeallocationCapacityPerProcessingUnit = new HashMap<ProcessingUnit, ClusterCapacityRequirements>();
        machineIsolationPerProcessingUnit = new HashMap<ProcessingUnit, ElasticProcessingUnitMachineIsolation>();
        timeoutTimestampPerAgentUidGoingDownPerProcessingUnit = new HashMap<ProcessingUnit,Map<String,Long>>();
        agentsStartedByMachineProvisioning = new HashSet<GridServiceAgent>();
    }

    public void initProcessingUnit(ProcessingUnit pu, final GridServiceAgent[] agents) {
        
        allocatedCapacityPerProcessingUnit.put(pu,new ClusterCapacityRequirements());
        markedForDeallocationCapacityPerProcessingUnit.put(pu, new ClusterCapacityRequirements());
        timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.put(pu,new HashMap<String,Long>());
        
        List<GridServiceAgentFutures> futures = new ArrayList<GridServiceAgentFutures>();
        futureAgentsPerProcessingUnit.put(pu, futures);
        if (agents.length > 0) {
            // the reason that we convert agents into futures
            // is to let the sla policy determine how to deal with these agents
            futures.add(convertToAgentFutures(agents));
        }
    }

    public void destroyProcessingUnit(ProcessingUnit pu) {
        machineIsolationPerProcessingUnit.remove(pu);
        allocatedCapacityPerProcessingUnit.remove(pu);
        futureAgentsPerProcessingUnit.remove(pu);
        markedForDeallocationCapacityPerProcessingUnit.remove(pu);
        timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.remove(pu);
    }

    public boolean isProcessingUnitDestroyed(ProcessingUnit pu) {
        return 
            allocatedCapacityPerProcessingUnit.get(pu) == null ||
            futureAgentsPerProcessingUnit.get(pu) == null ||
            markedForDeallocationCapacityPerProcessingUnit.get(pu) == null;
    }

    public void futureAgents(ProcessingUnit pu, FutureGridServiceAgent[] futureAgents, CapacityRequirements capacityRequirements) {
        this.futureAgentsPerProcessingUnit.get(pu).add(new GridServiceAgentFutures(futureAgents,capacityRequirements));
    }
    
    public void allocateCapacity(ProcessingUnit pu, String agentUid, CapacityRequirements capacity) {
        
        ClusterCapacityRequirements CapacityRequirements = allocatedCapacityPerProcessingUnit.get(pu);
        if (CapacityRequirements == null) {
            throw new IllegalArgumentException("pu");
        }
        
        allocatedCapacityPerProcessingUnit.put(
                pu,
                CapacityRequirements.add(agentUid,capacity));
        
    }

    public void markCapacityForDeallocation(ProcessingUnit pu, String agentUid, CapacityRequirements capacity) {
        
        ClusterCapacityRequirements CapacityRequirements = 
            allocatedCapacityPerProcessingUnit.get(pu);
        
        if (CapacityRequirements == null) {
            throw new IllegalArgumentException("pu");
        }
        
        ClusterCapacityRequirements deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
            
        allocatedCapacityPerProcessingUnit.put(pu,
                CapacityRequirements.subtract(agentUid,capacity));
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                deallocatedCapacity.add(agentUid, capacity));
    }
    
    public void unmarkCapacityForDeallocation(ProcessingUnit pu, String agentUid, CapacityRequirements capacity) {
        ClusterCapacityRequirements CapacityRequirements = 
            allocatedCapacityPerProcessingUnit.get(pu);
        
        if (CapacityRequirements == null) {
            throw new IllegalArgumentException("pu");
        }
        
        ClusterCapacityRequirements deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                deallocatedCapacity.subtract(agentUid, capacity));
        
        allocatedCapacityPerProcessingUnit.put(pu,
                CapacityRequirements.add(agentUid,capacity));
        
    }

    
    public void deallocateCapacity(ProcessingUnit pu, String agentUid, CapacityRequirements capacity) {
        
        ClusterCapacityRequirements deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                deallocatedCapacity.subtract(agentUid, capacity));
    }

    public ClusterCapacityRequirements getCapacityMarkedForDeallocation(ProcessingUnit pu) {
       return markedForDeallocationCapacityPerProcessingUnit.get(pu);
    }

    public ClusterCapacityRequirements getAllocatedCapacity(ProcessingUnit pu) {
        return allocatedCapacityPerProcessingUnit.get(pu);
    }
    
    public int getNumberOfFutureAgents(ProcessingUnit pu) {
        return this.futureAgentsPerProcessingUnit.get(pu).size();
    }

    public Collection<GridServiceAgentFutures> getFutureAgents(ProcessingUnit pu) {
        return Collections.unmodifiableCollection(this.futureAgentsPerProcessingUnit.get(pu));
    }

    public Collection<GridServiceAgentFutures> getAllDoneFutureAgents(ProcessingUnit pu) {
        
        final List<GridServiceAgentFutures> doneFutures = new ArrayList<GridServiceAgentFutures>();
        
        for (GridServiceAgentFutures future : futureAgentsPerProcessingUnit.get(pu)) {
            
            if (future.isDone()) {
                doneFutures.add(future);
            }
        }
        
        return doneFutures;        
    }
    
    /**
     * Lists all grid service agents from all processing units including those that are marked for deallocation.
     */
    public Collection<String> getAllUsedAgentUids() {
        
        return getAllUsedCapacity().getAgentUids();
    }
    
    /**
     * Lists all capacity from all processing units including those that are marked for deallocation.
     */
    public ClusterCapacityRequirements getAllUsedCapacity() {
        
        ClusterCapacityRequirements allUsedCapacity = new ClusterCapacityRequirements();
        
        for (ClusterCapacityRequirements CapacityRequirements : this.allocatedCapacityPerProcessingUnit.values()) {
            allUsedCapacity = allUsedCapacity.add(CapacityRequirements);
        }
        
        for (ClusterCapacityRequirements markedForDeallocationCapacity : this.markedForDeallocationCapacityPerProcessingUnit.values()) {
            allUsedCapacity = allUsedCapacity.add(markedForDeallocationCapacity);
        }
        
        return allUsedCapacity;
    }

    /**
     * @return true if processing units other than the specified PU, also use the specified agent. false otherwise.
     */
    public boolean isAgentSharedWithOtherProcessingUnits(ProcessingUnit pu, String agentUid) {
        
        for (ProcessingUnit otherPu : allocatedCapacityPerProcessingUnit.keySet()) {
            if (!otherPu.equals(pu) &&
                allocatedCapacityPerProcessingUnit.get(otherPu).getAgentUids().contains(agentUid)) {
                return true;
            }
        }
        
        for (ProcessingUnit otherPu : markedForDeallocationCapacityPerProcessingUnit.keySet()) {
            if (!otherPu.equals(pu) &&
                markedForDeallocationCapacityPerProcessingUnit.get(otherPu).getAgentUids().contains(agentUid)) {
                return true;
            }
        }
        return false;
    }

    public void agentGoingDown(ProcessingUnit pu, String agentUid, long timeout, TimeUnit unit) {
        timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.get(pu).put(agentUid, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    public void agentShutdownComplete(String agentUid) {
        for (Map<String, Long> agentsGoingDown : timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.values()) {
            if (agentsGoingDown.containsKey(agentUid)) {
                agentsGoingDown.remove(agentUid);
                return;
            }
        }
        
        throw new IllegalArgumentException("agentUid");
    }

    public Collection<String> getAgentUidsGoingDown(ProcessingUnit pu) {
        return Collections.unmodifiableCollection(new ArrayList<String>(this.timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.get(pu).keySet()));
    }
    
    public boolean isAgentUidGoingDownTimedOut(String agentUid) {
        
        for (Map<String, Long> agentsGoingDown : timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.values()) {
            if (agentsGoingDown.containsKey(agentUid)) {
                return agentsGoingDown.get(agentUid) < System.currentTimeMillis();
            }
        }
        
        throw new IllegalArgumentException("agentUid");
    }

    public void markAgentCapacityForDeallocation(ProcessingUnit pu, String uid) {
        CapacityRequirements agentCapacity = getAllocatedCapacity(pu).getAgentCapacity(uid);
        markCapacityForDeallocation(pu, uid,agentCapacity);
    }

    public void deallocateAgentCapacity(ProcessingUnit pu, String agentUid) {
        CapacityRequirements agentCapacity = getCapacityMarkedForDeallocation(pu).getAgentCapacity(agentUid);
        deallocateCapacity(pu, agentUid , agentCapacity);
        
    }

    /**
     * @return all Grid Service Agent UIDs that the specified PU cannot be deploy on due to machine isolation restrictions
     * or due to the fact that the machine is about to be deployed by another PU that started it.
     */
    public Collection<String> getRestrictedAgentUidsForPu(ProcessingUnit pu) {
        
        //find all PUs with different machine isolation, and same machine isolation
        Collection<ProcessingUnit> pusWithDifferentIsolation = new HashSet<ProcessingUnit>();
        Collection<ProcessingUnit> pusWithSameIsolation = new HashSet<ProcessingUnit>();
        ElasticProcessingUnitMachineIsolation puIsolation = machineIsolationPerProcessingUnit.get(pu);
        
        for (ProcessingUnit otherPu : machineIsolationPerProcessingUnit.keySet()) {
            
            ElasticProcessingUnitMachineIsolation otherPuIsolation = machineIsolationPerProcessingUnit.get(otherPu);
            if (otherPuIsolation.equals(puIsolation)) {
                pusWithSameIsolation.add(otherPu);
            }
            else {
                pusWithDifferentIsolation.add(otherPu);
            }
        }

        if (logger.isDebugEnabled()) {
            List<String> puNamesWithDifferentIsolation = new ArrayList<String>();
            for (ProcessingUnit puDifferentIsolation : pusWithDifferentIsolation) {
                puNamesWithDifferentIsolation.add(puDifferentIsolation.getName());
            }
            logger.debug("PUs with different isolation than pu " + pu.getName() +": "+ puNamesWithDifferentIsolation);
            
            List<String> puNamesWithSameIsolation = new ArrayList<String>();
            for (ProcessingUnit puSameIsolation : pusWithSameIsolation) {
                puNamesWithSameIsolation.add(puSameIsolation.getName());
            }
            logger.debug("PUs with same isolation of pu " + pu.getName() + ": " + puNamesWithSameIsolation);
        }
        
        // add all agent uids used by conflicting pus
        Set<String> restrictedAgentUids = new HashSet<String>();
        
        for (ProcessingUnit otherPu : pusWithDifferentIsolation) {
            restrictedAgentUids.addAll(allocatedCapacityPerProcessingUnit.get(otherPu).getAgentUids());
            restrictedAgentUids.addAll(markedForDeallocationCapacityPerProcessingUnit.get(otherPu).getAgentUids());
            restrictedAgentUids.addAll(timeoutTimestampPerAgentUidGoingDownPerProcessingUnit.get(otherPu).keySet());
        }
        
        // add all agents that started containers that are not with the same isolation
        Set<String> allowedContainerZones = new HashSet<String>();
        for (ProcessingUnit otherPu : pusWithSameIsolation) {
            allowedContainerZones.addAll(Arrays.asList(otherPu.getRequiredZones()));
        }
        for (GridServiceContainer container : pu.getAdmin().getGridServiceContainers()) {
            
            Set<String> containerZones = container.getZones().keySet();
            
            if (disjoint(containerZones,allowedContainerZones) && 
                container.getGridServiceAgent() != null) {
                
                restrictedAgentUids.add(container.getGridServiceAgent().getUid());
            }
        }
        
        // add all future grid service agents that have been started but not allocated yet
        for (List<GridServiceAgentFutures> futureAgentss : this.futureAgentsPerProcessingUnit.values()) {
            for (GridServiceAgentFutures futureAgents: futureAgentss) {
                for (GridServiceAgent agent : futureAgents.getGridServiceAgents()) {
                    restrictedAgentUids.add(agent.getUid());
                }
            }
        }
        
        return restrictedAgentUids;
   }

    /**
     * @return false only if a disjoints b (a and b have nothing in common) 
     * @param keySet
     * @param restrictedContainerZones
     * @return
     */
    private boolean disjoint(Set<String> a, Set<String> b) {
        Set<String> common = new HashSet<String>(a);
        common.retainAll(b);
        return common.size() == 0;
    }

    public void removeFutureAgents(ProcessingUnit pu, GridServiceAgentFutures futureAgents) {
        futureAgentsPerProcessingUnit.get(pu).remove(futureAgents);
    }
    
    

    public Collection<String> getAllUsedAgentUidsForPu(ProcessingUnit pu) {
        return allocatedCapacityPerProcessingUnit.get(pu).add(markedForDeallocationCapacityPerProcessingUnit.get(pu)).getAgentUids();
    }
    
    public static GridServiceAgentFutures convertToAgentFutures(final GridServiceAgent[] agents) {
        final Date timestamp = new Date(System.currentTimeMillis());
        FutureGridServiceAgent[] futureAgents = new FutureGridServiceAgent[agents.length];
        for (int i = 0 ; i < agents.length ; i++) {
            final GridServiceAgent agent = agents[i];
            futureAgents[i] =   
            new FutureGridServiceAgent() {
       
                public GridServiceAgent get() throws ExecutionException, IllegalStateException, TimeoutException {
                    return agent;
                }
       
                public boolean isDone() {
                    return true;
                }
                
                public boolean isTimedOut() {
                    return false;
                }
       
                public ExecutionException getException() {
                    return null;
                }
       
                public Date getTimestamp() {
                    return timestamp;
                }
       
                public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
                    return null;
                }

                public CapacityRequirements getFutureCapacity() {
                    return new MachineCapacityRequirements(agent.getMachine());
                }
            };
        }
        CapacityRequirements expectedCapacity = new CapacityRequirements(
                new NumberOfMachinesCapacityRequirement(agents.length));
        return new GridServiceAgentFutures(futureAgents, expectedCapacity);
    }

    public void removeIndicationThatAgentStartedByMachineProvisioning(GridServiceAgent agent) {
        this.agentsStartedByMachineProvisioning.remove(agent);
    }

    public void addIndicationThatAgentStartedByMachineProvisioning(GridServiceAgent agent) {
        agentsStartedByMachineProvisioning.add(agent);
    }
    
    public Collection<GridServiceAgent> getAgentsStartedByMachineProvisioning() {
        return Collections.unmodifiableCollection(new ArrayList<GridServiceAgent>(this.agentsStartedByMachineProvisioning));
    }
    
    public void setMachineIsolation(ProcessingUnit pu, ElasticProcessingUnitMachineIsolation isolation) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("PU " + pu.getName() + " machine isolation is " + isolation);
        }
        
        this.machineIsolationPerProcessingUnit.put(pu,isolation);
    }
    
    public ElasticProcessingUnitMachineIsolation getMachineIsolation(ProcessingUnit pu) {
        if (this.machineIsolationPerProcessingUnit.get(pu) == null) {
            throw new IllegalStateException("PU machine isolation has not been defined");
        }
        return this.machineIsolationPerProcessingUnit.get(pu);
    }
}
