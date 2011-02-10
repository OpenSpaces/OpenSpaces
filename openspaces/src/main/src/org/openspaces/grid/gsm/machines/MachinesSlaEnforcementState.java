package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;

class MachinesSlaEnforcementState {
    
    // state that tracks managed grid service agents, agents to be started and agents marked for shutdown.
    private final Map<ProcessingUnit,AggregatedAllocatedCapacity> allocatedCapacityPerProcessingUnit;
    private final Map<ProcessingUnit,List<FutureGridServiceAgents>> futureAgentsPerProcessingUnit;
    private final Map<ProcessingUnit,AggregatedAllocatedCapacity> markedForDeallocationCapacityPerProcessingUnit;

    MachinesSlaEnforcementState() {
        allocatedCapacityPerProcessingUnit = new HashMap<ProcessingUnit, AggregatedAllocatedCapacity>();
        futureAgentsPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureGridServiceAgents>>();
        markedForDeallocationCapacityPerProcessingUnit = new HashMap<ProcessingUnit, AggregatedAllocatedCapacity>();
        
    }

    public void initProcessingUnit(ProcessingUnit pu, final GridServiceAgent[] agents) {
        allocatedCapacityPerProcessingUnit.put(pu,new AggregatedAllocatedCapacity());
        markedForDeallocationCapacityPerProcessingUnit.put(pu, new AggregatedAllocatedCapacity());
        
        List<FutureGridServiceAgents> futures = new ArrayList<FutureGridServiceAgents>();
        futureAgentsPerProcessingUnit.put(pu, futures);
        if (agents.length > 0) {
            // the reason that we convert agents into futures
            // is to let the sla policy determine how to deal with these agents
            futures.add(convertToFutureAgents(agents));
        }
    }

    private FutureGridServiceAgents convertToFutureAgents(final GridServiceAgent[] agents) {
        final Date timestamp = new Date(System.currentTimeMillis());
        FutureGridServiceAgents futureAgents = 
            new FutureGridServiceAgents() {
   
            public GridServiceAgent[] get() throws ExecutionException, IllegalStateException, TimeoutException {
                return agents;
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
   
            public CapacityRequirements getCapacityRequirements() {
                return new CapacityRequirements(new NumberOfMachinesCapacityRequirement(agents.length));
            }
        };
        return futureAgents;
    }

    public void destroyProcessingUnit(ProcessingUnit pu) {
        allocatedCapacityPerProcessingUnit.remove(pu);
        futureAgentsPerProcessingUnit.remove(pu);
        markedForDeallocationCapacityPerProcessingUnit.remove(pu);
    }

    public boolean isProcessingUnitDestroyed(ProcessingUnit pu) {
        return 
            allocatedCapacityPerProcessingUnit.get(pu) == null ||
            futureAgentsPerProcessingUnit.get(pu) == null ||
            markedForDeallocationCapacityPerProcessingUnit.get(pu) == null;
    }

    public void futureAgent(ProcessingUnit pu, FutureGridServiceAgents futureMachine) {
        this.futureAgentsPerProcessingUnit.get(pu).add(futureMachine);
    }
    
    public void allocateCapacity(ProcessingUnit pu, String agentUid, AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity allocatedCapacity = allocatedCapacityPerProcessingUnit.get(pu);
        if (allocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        allocatedCapacityPerProcessingUnit.put(pu,
                AggregatedAllocatedCapacity.add(allocatedCapacity,agentUid,capacity));
        
    }

    public void markCapacityForDeallocation(ProcessingUnit pu, String agentUid, AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity allocatedCapacity = 
            allocatedCapacityPerProcessingUnit.get(pu);
        
        if (allocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        AggregatedAllocatedCapacity deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
            
        allocatedCapacityPerProcessingUnit.put(pu,
                AggregatedAllocatedCapacity.subtract(allocatedCapacity,agentUid,capacity));
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                    AggregatedAllocatedCapacity.add(deallocatedCapacity, agentUid, capacity));
    }
    
    public void unmarkCapacityForDeallocation(ProcessingUnit pu, String agentUid, AllocatedCapacity capacity) {
        AggregatedAllocatedCapacity allocatedCapacity = 
            allocatedCapacityPerProcessingUnit.get(pu);
        
        if (allocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        AggregatedAllocatedCapacity deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                AggregatedAllocatedCapacity.subtract(deallocatedCapacity, agentUid, capacity));
        
        allocatedCapacityPerProcessingUnit.put(pu,
                AggregatedAllocatedCapacity.add(allocatedCapacity,agentUid,capacity));
        
    }

    
    public void deallocateCapacity(ProcessingUnit pu, String agentUid, AllocatedCapacity capacity) {
        
        AggregatedAllocatedCapacity deallocatedCapacity = 
            markedForDeallocationCapacityPerProcessingUnit.get(pu);
        
        if (deallocatedCapacity == null) {
            throw new IllegalArgumentException("pu");
        }
        
        markedForDeallocationCapacityPerProcessingUnit.put(pu,
                AggregatedAllocatedCapacity.subtract(deallocatedCapacity, agentUid, capacity));
    }

    public AggregatedAllocatedCapacity getCapacityMarkedForDeallocation(ProcessingUnit pu) {
       return markedForDeallocationCapacityPerProcessingUnit.get(pu);
    }

    public AggregatedAllocatedCapacity getAllocatedCapacity(ProcessingUnit pu) {
        return allocatedCapacityPerProcessingUnit.get(pu);
    }
    
    public int getNumberOfFutureAgents(ProcessingUnit pu) {
        return this.futureAgentsPerProcessingUnit.get(pu).size();
    }

    public Collection<FutureGridServiceAgents> getFutureAgents(ProcessingUnit pu) {
        return Collections.unmodifiableCollection(this.futureAgentsPerProcessingUnit.get(pu));
    }

    public Collection<FutureGridServiceAgents> removeAllDoneFutureAgents(ProcessingUnit pu) {
        
        final List<FutureGridServiceAgents> doneFutures = new ArrayList<FutureGridServiceAgents>();
        
        final Iterator<FutureGridServiceAgents> iterator = this.futureAgentsPerProcessingUnit.get(pu).iterator();
        while (iterator.hasNext()) {
            FutureGridServiceAgents future = iterator.next();
            
            if (future.isDone()) {
            
                // remove future from futureAgents list since it is done.
                iterator.remove();
                doneFutures.add(future);
            }
        }
        
        return doneFutures;        
    }

    /**
     * Lists all grid service agents from all processing units including those that are pending shutdown.
     * This method is unique since it reads state from all endpoints.
     */
    public Set<String> getAllUsedAgentUids() {
        
        Set<String> agentUids = new HashSet<String>();
        
        for (AggregatedAllocatedCapacity allocatedCapacity : this.allocatedCapacityPerProcessingUnit.values()) {
            agentUids.addAll(allocatedCapacity.getAgentUids());
        }
        
        for (AggregatedAllocatedCapacity markedForDeallocationCapacity : this.markedForDeallocationCapacityPerProcessingUnit.values()) {
            agentUids.addAll(markedForDeallocationCapacity.getAgentUids());
        }
        
        return agentUids;
    }
}
