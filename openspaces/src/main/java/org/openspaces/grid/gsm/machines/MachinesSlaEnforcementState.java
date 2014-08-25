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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GSAReservationId;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.zone.config.ZonesConfigUtils;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.machines.backup.MachinesState;
import org.openspaces.grid.gsm.machines.exceptions.UndeployInProgressException;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.PublicMachineIsolation;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.internal.version.PlatformLogicalVersion;

public class MachinesSlaEnforcementState {
    
    public static class StateKey implements Comparable<StateKey>{
        
        ProcessingUnit pu;
        ZonesConfig gridServiceAgentZones;
        
        public StateKey (ProcessingUnit pu, ZonesConfig gridServiceAgentZones) {
            this.pu = pu;
            this.gridServiceAgentZones = gridServiceAgentZones;

        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((gridServiceAgentZones == null) ? 0 : gridServiceAgentZones.hashCode());
            result = prime * result + ((pu == null) ? 0 : pu.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StateKey other = (StateKey) obj;
            if (gridServiceAgentZones == null) {
                if (other.gridServiceAgentZones != null)
                    return false;
            } else if (!gridServiceAgentZones.equals(other.gridServiceAgentZones))
                return false;
            if (pu == null) {
                if (other.pu != null)
                    return false;
            } else if (!pu.equals(other.pu))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StateKey ["
                    + (pu != null ? "pu=" + pu.getName() + ", " : "")
                    + (gridServiceAgentZones != null ? "agentZones=" + gridServiceAgentZones : "")
                    + "]";
        }

        @Override
        public int compareTo(StateKey o) {
            return this.toString().compareTo(o.toString());
        }
    }
    
    class StateValue {
        
        private CapacityRequirementsPerAgent allocatedCapacity = new CapacityRequirementsPerAgent();
        private final List<GridServiceAgentFutures> futureAgents = new ArrayList<GridServiceAgentFutures>();
        private CapacityRequirementsPerAgent markedForDeallocationCapacity = new CapacityRequirementsPerAgent();
        private ElasticProcessingUnitMachineIsolation machineIsolation;
        private List<FutureStoppedMachine> machinesBeingStopped = new ArrayList<FutureStoppedMachine>();
        private boolean completedStateRecoveryAfterRestart;
        private List<RecoveringFailedGridServiceAgent> failedAgents = new ArrayList<RecoveringFailedGridServiceAgent>();
        
        public void addFutureStoppedMachine(FutureStoppedMachine futureStoppedMachine) {
            machinesBeingStopped.add(futureStoppedMachine);
            machinesStateVersion++;
        }
        
        public void removeFutureStoppedMachine(FutureStoppedMachine futureStoppedMachine) {
            machinesBeingStopped.remove(futureStoppedMachine);
            machinesStateVersion++;
        }
        
        public Collection<FutureStoppedMachine> getMachineGoingDown() {
            return Collections.unmodifiableList(new ArrayList<FutureStoppedMachine>(this.machinesBeingStopped));
        }
        
        public void addFutureAgents(FutureGridServiceAgent[] newFutureAgents, CapacityRequirements capacityRequirements) {
            futureAgents.add(new GridServiceAgentFutures(newFutureAgents,capacityRequirements));
            machinesStateVersion++;
        }

        public void allocateCapacity(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before allocating capacity");
            }
            MachinesSlaEnforcementState.this.logger.trace("Adding {" + agentUid + ", " + capacity + "} to allocatedCapacity = " + allocatedCapacity.toDetailedString());
            allocatedCapacity = allocatedCapacity.add(agentUid,capacity);
            machinesStateVersion++;
        }

        public void markCapacityForDeallocation(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before marking capacity for de-allocation");
            }
            MachinesSlaEnforcementState.this.logger.trace("Subtracting {" + agentUid + ", " + capacity + "} from allocatedCapacity = " + allocatedCapacity);
            allocatedCapacity = allocatedCapacity.subtract(agentUid,capacity);
            MachinesSlaEnforcementState.this.logger.trace("Adding {" + agentUid + ", " + capacity + "} to markedForDeallocationCapacity = " + markedForDeallocationCapacity);
            markedForDeallocationCapacity = markedForDeallocationCapacity.add(agentUid, capacity);
            machinesStateVersion++;
        }

        public void unmarkCapacityForDeallocation(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before un-marking capacity for de-allocation");
            }
            MachinesSlaEnforcementState.this.logger.trace("Subtracting {" + agentUid + ", " + capacity + "} from markedForDeallocationCapacity = " + markedForDeallocationCapacity);
            markedForDeallocationCapacity = markedForDeallocationCapacity.subtract(agentUid, capacity);
            allocateCapacity(agentUid, capacity);
            machinesStateVersion++;
        }

        public void deallocateCapacity(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before de-allocating capacity");
            }
            MachinesSlaEnforcementState.this.logger.trace("Subtracting {" + agentUid + ", " + capacity + "} from markedForDeallocationCapacity = " + markedForDeallocationCapacity);
            markedForDeallocationCapacity = markedForDeallocationCapacity.subtract(agentUid, capacity);
            machinesStateVersion++;
        }

		public void replaceAllocation(String oldAgentUid, String newAgentUid) {
			final CapacityRequirements agentDeallocationCapacity = markedForDeallocationCapacity.getAgentCapacityOrZero(oldAgentUid);
			if (!agentDeallocationCapacity.equalsZero()) {
				markedForDeallocationCapacity = markedForDeallocationCapacity.subtractAgent(oldAgentUid).add(newAgentUid, agentDeallocationCapacity);
			}

			final CapacityRequirements agentAllocatedCapacity = allocatedCapacity.getAgentCapacityOrZero(oldAgentUid);
			if (!agentAllocatedCapacity.equalsZero()) {
				allocatedCapacity = allocatedCapacity.subtractAgent(oldAgentUid);
				allocateCapacity(newAgentUid, agentAllocatedCapacity);
			}
            machinesStateVersion++;
		}
		
        public Collection<GridServiceAgentFutures> getAllDoneFutureAgents() {
            final List<GridServiceAgentFutures> doneFutures = new ArrayList<GridServiceAgentFutures>();
            
            for (GridServiceAgentFutures future : futureAgents) {
                
                if (future.isDone()) {
                    doneFutures.add(future);
                }
            }
            
            return doneFutures;
        }

        public void removeFutureAgents(GridServiceAgentFutures futureAgentsToRemove) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before removing future agent");
            }
            futureAgents.remove(futureAgentsToRemove);
            machinesStateVersion++;
        }

        public void completedStateRecoveryAfterRestart() {
            completedStateRecoveryAfterRestart = true;
        }

        @Override
        public String toString() {
            return "StateValue ["
                    + (allocatedCapacity != null ? "allocatedCapacity=" + allocatedCapacity + ", " : "")
                    + (futureAgents != null ? "futureAgents=" + futureAgents + ", " : "")
                    + (markedForDeallocationCapacity != null ? "markedForDeallocationCapacity="
                            + markedForDeallocationCapacity + ", " : "")
                    + (machineIsolation != null ? "machineIsolation=" + machineIsolation + ", " : "")
                    + "completedStateRecoveryAfterRestart="
                    + completedStateRecoveryAfterRestart + ", " 
                    + "failedAgents=" + failedAgents + "]";
        }

        public boolean equalsZero() {
            return allocatedCapacity.equalsZero() 
                   && markedForDeallocationCapacity.equalsZero() 
                   && futureAgents.isEmpty();
        }

		public Collection<RecoveringFailedGridServiceAgent> getFailedAgents() {
			return Collections.unmodifiableCollection(failedAgents);
		}

		public void addFailedAgent(RecoveringFailedGridServiceAgent failedAgent) {
			failedAgents.add(failedAgent);
			machinesStateVersion++;
		}

		public void removeFailedAgent(String agentUid) {
		    final Iterator<RecoveringFailedGridServiceAgent> it = failedAgents.iterator();
            while (it.hasNext()) {
                if (it.next().getAgentUid().equals(agentUid)) {
                    it.remove();
                }
            }
			machinesStateVersion++;
		}
    }
    
    private final Log logger;
    
    private final Map<StateKey,StateValue> state;

    public enum RecoveryState {
        NOT_RECOVERED, RECOVERY_SUCCESS, RECOVERY_FAILED
    }
    
    private final Map<ProcessingUnit, RecoveryState> recoveredStatePerProcessingUnit;
    private final Set<ProcessingUnit> validatedUndeployNotInProgressPerProcessingUnit;
    private final Map<ProcessingUnit, FutureCleanupCloudResources> cloudCleanupPerProcessingUnit;
    private final Map<String, String> agentWithFailoverDisabledPerIpAddress;
    private final Map<String, Object> agentsContext;
    private long machinesStateVersion;
    
    public MachinesSlaEnforcementState() {
        this.logger =
                new SingleThreadedPollingLog( 
                        LogFactory.getLog(MachinesSlaEnforcementState.class));
        
        state = new HashMap<StateKey,StateValue>();
        recoveredStatePerProcessingUnit = new HashMap<ProcessingUnit, MachinesSlaEnforcementState.RecoveryState>();
        validatedUndeployNotInProgressPerProcessingUnit = new HashSet<ProcessingUnit>();
        cloudCleanupPerProcessingUnit = new HashMap<ProcessingUnit, FutureCleanupCloudResources>();
        agentWithFailoverDisabledPerIpAddress = new HashMap<String, String>();
        agentsContext = new LinkedHashMap<String,Object>();
        machinesStateVersion = 0;
    }

    public boolean isHoldingStateForProcessingUnit(ProcessingUnit pu) {
        return !getGridServiceAgentsZones(pu).isEmpty();
    }

    private StateValue getState(StateKey key) {
        if (!state.containsKey(key)) {
            state.put(key, new StateValue());
        }
        return state.get(key);
    }
    
    public void addFutureAgents(StateKey key, FutureGridServiceAgent[] futureAgents, CapacityRequirements capacityRequirements) {
        getState(key).addFutureAgents(futureAgents, capacityRequirements);
    }
    

    public void allocateCapacity(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).allocateCapacity(agentUid, capacity);
    }

    public void markCapacityForDeallocation(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).markCapacityForDeallocation(agentUid, capacity);
    }
    
    public void unmarkCapacityForDeallocation(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).unmarkCapacityForDeallocation(agentUid, capacity);        
    }

    
    public void deallocateCapacity(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).deallocateCapacity(agentUid, capacity);
    }

    public CapacityRequirementsPerAgent getCapacityMarkedForDeallocation(StateKey key) {
       return getState(key).markedForDeallocationCapacity;
    }

    public CapacityRequirementsPerAgent getAllocatedCapacity(StateKey key) {
        return getState(key).allocatedCapacity;
    }
    
    public CapacityRequirementsPerAgent getAllocatedCapacityOfOtherKeysFromSamePu(StateKey key) {
        CapacityRequirementsPerAgent capacityRequirementsPerAgent =  new CapacityRequirementsPerAgent();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            StateKey otherKey = pair.getKey();
            if (otherKey.pu.equals(key.pu) && 
                !otherKey.gridServiceAgentZones.equals(key.gridServiceAgentZones)) {
                //same pu, different agent zone
                CapacityRequirementsPerAgent otherAllocatedCapacity = getAllocatedCapacity(otherKey);
                capacityRequirementsPerAgent = capacityRequirementsPerAgent.add(otherAllocatedCapacity);
            }
        }
        return capacityRequirementsPerAgent;
    }
    
    public int getNumberOfFutureAgents(StateKey key) {
        return getState(key).futureAgents.size();
    }

    public Collection<GridServiceAgentFutures> getFutureAgents(StateKey key) {
        return Collections.unmodifiableCollection(getState(key).futureAgents);
    }

    public Collection<GridServiceAgentFutures> getAllDoneFutureAgents(StateKey key) {
        return getState(key).getAllDoneFutureAgents();
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
    public CapacityRequirementsPerAgent getAllUsedCapacity() {
        
        CapacityRequirementsPerAgent allUsedCapacity = new CapacityRequirementsPerAgent();
        
        for (StateKey key: state.keySet()) {
            allUsedCapacity = allUsedCapacity.add(getAllUsedCapacity(key));
        }
        
        return allUsedCapacity;
    }

    /**
     * Lists all capacity from all processing units including those that are marked for deallocation for specified key.
     */
    private CapacityRequirementsPerAgent getAllUsedCapacity(StateKey key) {
        final StateValue value = getState(key);
        return (value.allocatedCapacity).add(value.markedForDeallocationCapacity);
    }

    /**
     * @return true if processing units other than the specified PU, also use the specified agent. false otherwise.
     */
    public boolean isAgentSharedWithOtherProcessingUnits(ProcessingUnit pu, String agentUid) {
        
        for (Entry<StateKey, StateValue>  pair : state.entrySet()) {
            if (pair.getKey().pu.equals(pu)) {
                continue;
            }

            StateValue value = pair.getValue();
            if (!value.allocatedCapacity.getAgentCapacityOrZero(agentUid).equalsZero() ||
                !value.markedForDeallocationCapacity.getAgentCapacityOrZero(agentUid).equalsZero()) {
                return true;
            }
        }
        return false;
    }
    
    public Collection<FutureStoppedMachine> getMachinesGoingDown(StateKey key) {
        return getState(key).getMachineGoingDown();
    }

	public void markAgentAsFailed(StateKey key, String agentUid) {
		markAgentCapacityForDeallocation(key, agentUid);
		addFailedAgent(key, agentUid);
	}
	
	public void markAgentRestrictedForPu(StateKey key, String agentUid) {
		markAgentCapacityForDeallocation(key, agentUid);
	}

	private void markAgentCapacityForDeallocation(StateKey key, String uid) {
        CapacityRequirements agentCapacity = getAllocatedCapacity(key).getAgentCapacity(uid);
        markCapacityForDeallocation(key, uid,agentCapacity);
    }

    public void deallocateAgentCapacity(StateKey key, String agentUid) {
        CapacityRequirements agentCapacity = getCapacityMarkedForDeallocation(key).getAgentCapacity(agentUid);
        deallocateCapacity(key, agentUid , agentCapacity);
    }

    /**
     * @param exactZones - the exact zones that the grid service agent should have
     * @return all Grid Service Agent UIDs that the specified PU cannot be deploy on due to machine isolation restrictions
     * or due to the fact that the machine is about to be deployed by another PU that started it.
     * The map keys contain the agent UIDs, and the map values contains the reasons for the restriction.
     */
    public Map<String,List<String>> getRestrictedAgentUids(StateKey key) {
        
        Admin admin = key.pu.getAdmin();
        ElasticProcessingUnitMachineIsolation puIsolation = getState(key).machineIsolation;
        Map<String,List<String>> restrictedAgentUidsWithReason = new HashMap<String,List<String>>();
         if (!(puIsolation instanceof PublicMachineIsolation)) {
             //find all PUs with different machine isolation, and same machine isolation
             final Collection<StateKey> keysWithDifferentIsolation = getKeysWithDifferentIsolation(key);
             final Collection<StateKey> keysWithSameIsolation = getKeysWithSameIsolation(key);

             for (StateKey otherKey: keysWithDifferentIsolation) {

                 StateValue otherValue = getState(otherKey);

                 for (String agentUid : otherValue.allocatedCapacity.getAgentUids()) {
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " allocated on machine which restricts  " + key.pu.getName() + " machineIsolation="+getState(key).machineIsolation);
                 }

                 for (String agentUid : otherValue.markedForDeallocationCapacity.getAgentUids()) {
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " marked for deallocation on machine which restricts  " + key.pu.getName() + " machineIsolation="+getState(key).machineIsolation);
                 }

                 for (FutureStoppedMachine futureStoppedMachine : otherValue.getMachineGoingDown()) {
                     GridServiceAgent agent = futureStoppedMachine.getGridServiceAgent();
                     initValue(restrictedAgentUidsWithReason, agent.getUid());
                     restrictedAgentUidsWithReason.get(agent.getUid()).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " is shutting down the agent which restricts  " + key.pu.getName() + " machineIsolation="+getState(key).machineIsolation);
                 }
             }

             // add all agents that started containers that are not with the same isolation
             Set<ZonesConfig> allowedContainerZoness = new HashSet<ZonesConfig>();
             for (StateKey otherKey : keysWithSameIsolation) {
                 allowedContainerZoness.add(otherKey.pu.getRequiredContainerZones());
             }
             for (GridServiceContainer container : admin.getGridServiceContainers()) {
                 if (container.getGridServiceAgent() == null) {
                     // ignore manually started containers using gsc.bat
                     continue;
                 }
                 boolean allowed = false;
                 for (ZonesConfig allowedContainerZones : allowedContainerZoness) {
                     if (container.getExactZones().isStasfies(allowedContainerZones)) {
                         allowed = true;
                         break;
                     }
                 }
                 if (!allowed) {
                     String agentUid = container.getGridServiceAgent().getUid();
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add("Machine has a container with restricted zones " + ContainersSlaUtils.gscToString(container));
                 }
             }
        }
        
        // add all future grid service agents that have been started but not allocated yet
        Map<GSAReservationId, Collection<GridServiceAgent>> agentsByReservationId = ((InternalGridServiceAgents)admin.getGridServiceAgents()).getAgentsGroupByReservationId();
        Map<GSAReservationId, StateKey> futureAgentsReservationIds = getFutureAgentsReservationIds();
        for (Entry<GSAReservationId, StateKey> pair : futureAgentsReservationIds.entrySet()) {
            GSAReservationId reservationId = pair.getKey();
            StateKey startedTheAgent = pair.getValue();
            Collection<GridServiceAgent> reservedAgents = agentsByReservationId.get(reservationId);
            if (reservedAgents != null) {
                for (GridServiceAgent agent : reservedAgents) {
                    String agentUid = agent.getUid();
                    initValue(restrictedAgentUidsWithReason, agentUid);
                    restrictedAgentUidsWithReason.get(agentUid).add("Agent has been started by " + startedTheAgent +" but not allocated yet. ReservationID=" + reservationId);
                }
            }
        }
        
        if (key.gridServiceAgentZones != null) {
            //add all agents that do not have this specific zone
            //notice that unlike MachinesSlaUtils#zoneFilter that only validates MachineProvisioning.getGSAZones()
            //this check is more restrictive. The machine is ok, just we cannot deploy on it with the given key
            for (GridServiceAgent agent : admin.getGridServiceAgents()) {
                if (!agent.getExactZones().isStasfies(key.gridServiceAgentZones)) {
                    String agentUid = agent.getUid();
                    initValue(restrictedAgentUidsWithReason, agentUid);
                    restrictedAgentUidsWithReason.get(agentUid).add("Agent zones=" + agent.getExactZones().getZones() +" does not match " + key.gridServiceAgentZones);
                }
            }
        }
        
        return restrictedAgentUidsWithReason;
   }

   private Collection<StateKey> getKeysWithSameIsolation(StateKey key) {
        
       final ElasticProcessingUnitMachineIsolation puIsolation = getState(key).machineIsolation;
       final Collection<StateKey> keysWithSameIsolation = new HashSet<StateKey>();

       for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            ElasticProcessingUnitMachineIsolation otherPuIsolation = pair.getValue().machineIsolation;
            if (otherPuIsolation == null) {
                throw new IllegalStateException(pair.getKey() + " should have set machine isolation");
            }
           if (otherPuIsolation.equals(puIsolation)) {
                keysWithSameIsolation.add(pair.getKey());
            }
        }
         
        if (logger.isDebugEnabled()) {
            logger.debug("PUs with same isolation of " + key + " are: " + keysWithSameIsolation);
        }
         
        return keysWithSameIsolation;
    }

    private Collection<StateKey> getKeysWithDifferentIsolation(StateKey key) {
            
        final ElasticProcessingUnitMachineIsolation puIsolation = getState(key).machineIsolation;
        final Collection<StateKey> keysWithDifferentIsolation = new HashSet<StateKey>();
        for (final Entry<StateKey, StateValue> pair : state.entrySet()) {
             final ElasticProcessingUnitMachineIsolation otherPuIsolation = pair.getValue().machineIsolation;
             if (otherPuIsolation == null) {
                 throw new IllegalStateException(pair.getKey() + " should have set machine isolation");
             }
             if (!otherPuIsolation.equals(puIsolation)) {
                 keysWithDifferentIsolation.add(pair.getKey());
             }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("PUs with different isolation than pu " + key +" are: "+ keysWithDifferentIsolation);
        }

        return keysWithDifferentIsolation;
    }
    
    private Map<GSAReservationId, StateKey> getFutureAgentsReservationIds() {
        Map<GSAReservationId, StateKey> reservationIds = new HashMap<GSAReservationId, MachinesSlaEnforcementState.StateKey>();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            for (GridServiceAgentFutures futureAgents: pair.getValue().futureAgents) {
                for (GSAReservationId reservationId : futureAgents.getReservationIds()) {
                    reservationIds.put(reservationId, pair.getKey());
                }
            }
        }
        return reservationIds;
    }

    private void initValue(Map<String, List<String>> mapOfLists, String key) {
        if (!mapOfLists.containsKey(key)) {
            mapOfLists.put(key, new LinkedList<String>());
        }
    }

    public void removeSuccesfullyStartedFutureAgents(StateKey key, GridServiceAgentFutures doneFutureAgents) {
        getState(key).removeFutureAgents(doneFutureAgents);

    	for (final FutureGridServiceAgent doneFutureAgent : doneFutureAgents.getFutureGridServiceAgents()) {

    		final FailedGridServiceAgent failedAgent = doneFutureAgent.getFailedGridServiceAgent();
			if (failedAgent != null) {
			
				// remove failed agent, since the new machine replaces it
				final String failedAgentUid = failedAgent.getAgentUid();
                for (StateKey okey : state.keySet()) {
                    unmarkAgentAsFailed(okey, failedAgentUid);
                }
				removeAgentContext(failedAgentUid);
			}

			//store agent context, so it could be resurrected if fails
			try {
				final String agentUid = doneFutureAgent.get().getAgent().getUid();
				final Object agentContext = doneFutureAgent.get().getAgentContext();
				addAgentContext(agentUid, agentContext);
			} catch (ExecutionException e) { 
				throw new IllegalStateException(e); 
			} catch (TimeoutException e) { 
				throw new IllegalStateException(e); 
			}

		}
    }
    
	public void unmarkAgentAsFailed(StateKey key, String agentUid) {
		getState(key).removeFailedAgent(agentUid);
	}

    private void addAgentContext(String agentUid, Object agentContext) {
        agentsContext.put(agentUid, agentContext);
        machinesStateVersion++;
    }
    
    public Object getAgentContext(String agentUid) {
        return agentsContext.get(agentUid);
    }
    
    public void removeFutureStoppedMachine(StateKey key, FutureStoppedMachine futureStoppedMachine) {
        getState(key).removeFutureStoppedMachine(futureStoppedMachine);

        final String agentUid = futureStoppedMachine.getGridServiceAgent().getUid();
        removeAgentContext(agentUid);
    }

    private void removeAgentContext(final String agentUid) {
        agentsContext.remove(agentUid);
        machinesStateVersion++;
    }
    
    public Collection<FutureStoppedMachine> getMachinesGoingDown() {
        
        List<FutureStoppedMachine> machinesGoingDown = new ArrayList<FutureStoppedMachine>();
        for (StateValue value : state.values()) {
            machinesGoingDown.addAll(value.getMachineGoingDown());
        }
        return Collections.unmodifiableList(new ArrayList<FutureStoppedMachine>(machinesGoingDown));
    }
    
    public void addFutureStoppedMachine(StateKey key, FutureStoppedMachine futureStoppedMachine) {
        getState(key).addFutureStoppedMachine(futureStoppedMachine);
    }
    
    public Collection<String> getUsedAgentUids(StateKey key) {
        StateValue stateValue = getState(key);
        return stateValue.allocatedCapacity.add(stateValue.markedForDeallocationCapacity)
               .getAgentUids();
    }
    
    public void setMachineIsolation(StateKey key, ElasticProcessingUnitMachineIsolation isolation) {
        
        if (isolation == null) {
            throw new IllegalArgumentException("machine isolation cannot be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(key + " machine isolation is " + isolation);
        }
        getState(key).machineIsolation = isolation;
    }
    
    public ElasticProcessingUnitMachineIsolation getMachineIsolation(StateKey key) {
        ElasticProcessingUnitMachineIsolation machineIsolation = getState(key).machineIsolation;
        if (machineIsolation == null) {
            throw new IllegalStateException(key + " machine isolation has not been defined");
        }
        return machineIsolation;
    }

    public boolean isCompletedStateRecovery(StateKey key) {
        return getState(key).completedStateRecoveryAfterRestart;
    }

    public void completedStateRecovery(StateKey key) {
        getState(key).completedStateRecoveryAfterRestart();      
    }

    public void recoveredStateOnEsmStart(ProcessingUnit otherPu) {
        recoveredStatePerProcessingUnit.put(otherPu,RecoveryState.RECOVERY_SUCCESS);
    }
    
    public void failedRecoveredStateOnEsmStart(ProcessingUnit otherPu) {
        recoveredStatePerProcessingUnit.put(otherPu,RecoveryState.RECOVERY_FAILED);
    }

    public RecoveryState getRecoveredStateOnEsmStart(ProcessingUnit pu) {
        RecoveryState recoveryState = recoveredStatePerProcessingUnit.get(pu);
        if (recoveryState == null) {
            recoveryState = RecoveryState.NOT_RECOVERED;
        }
        return recoveryState; 
    }

    public Set<ZonesConfig> getGridServiceAgentsZones(ProcessingUnit pu) {
        Set<ZonesConfig> zones = new HashSet<ZonesConfig>();
        for (StateKey key : getStateForProcessingUnit(pu).keySet()) {
            zones.add(key.gridServiceAgentZones);
        }
        return zones;
    }
    
    public Set<ZonesConfig> getUndeployedGridServiceAgentsZones(ProcessingUnit pu) { 
        Set<ZonesConfig> zones = new HashSet<ZonesConfig>();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            if (pair.getKey().pu.equals(pu)) {
                if (pair.getValue().equalsZero()) {
                    zones.add(pair.getKey().gridServiceAgentZones);
                }
            }
        }
        return zones;
    }
    
    public Map<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> groupCapacityPerProcessingUnitPerAgent(StateKey key) {
        
        // create a report for each relevant agent - which pus are installed on it and how much capacity they are using
        final Map<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>> capacityPerPuPerAgent = new HashMap<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>>();
        Admin admin = key.pu.getAdmin();
        Collection<String> restrictedAgentUids = getRestrictedAgentUids(key).keySet();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            ProcessingUnit otherPu = pair.getKey().pu;
            CapacityRequirementsPerAgent otherPuCapacityPerAgents = pair.getValue().allocatedCapacity;
            for (String agentUid : otherPuCapacityPerAgents.getAgentUids()) {
                GridServiceAgent agent = admin.getGridServiceAgents().getAgentByUID(agentUid);
                if (!restrictedAgentUids.contains(agentUid) && agent != null) {
                    
                    if (!capacityPerPuPerAgent.containsKey(agent)) {
                        //lazy init
                        capacityPerPuPerAgent.put(agent, new HashMap<ProcessingUnit, CapacityRequirements>());
                    }
                    
                    if (!capacityPerPuPerAgent.get(agent).containsKey(otherPu)) {
                        capacityPerPuPerAgent.get(agent).put(otherPu, new CapacityRequirements());
                    }
                     
                    CapacityRequirements otherPuCapacityOnAgent = capacityPerPuPerAgent.get(agent).get(otherPu);
                    CapacityRequirements otherPuCapacityOnAgentIncrease = otherPuCapacityPerAgents.getAgentCapacity(agentUid);
                    otherPuCapacityOnAgent = otherPuCapacityOnAgent.add(otherPuCapacityOnAgentIncrease);
                    capacityPerPuPerAgent.get(agent).put(otherPu, otherPuCapacityOnAgent);
                }
            }
        }
        return capacityPerPuPerAgent;
    }
    
    public CapacityRequirementsPerAgent getAllocatedCapacity(ProcessingUnit otherPu) {
        CapacityRequirementsPerAgent capacityRequirementsPerAgent =  new CapacityRequirementsPerAgent();
        for (ZonesConfig  zones : getGridServiceAgentsZones(otherPu)) {
            capacityRequirementsPerAgent = capacityRequirementsPerAgent.add(getAllocatedCapacity(new StateKey(otherPu,zones)));
        }
        return capacityRequirementsPerAgent;
    }
    
    /**
     * Changes the key.zone of the allocated capacity to match the exact zone of the agent
     * @return false if nothing changed, true if replace occurred
     */
    public boolean replaceAllocatedCapacity(StateKey key, Admin admin) {
        boolean changed = false;
        final CapacityRequirementsPerAgent allocatedCapacityPerAgent  = getState(key).allocatedCapacity;
        Collection<String> agentUids = new ArrayList<String>(allocatedCapacityPerAgent.getAgentUids()); //copy before iteration
        for (String agentUid : agentUids) {
            final GridServiceAgent agent = admin.getGridServiceAgents().getAgentByUID(agentUid);
            //Agent could be null if it failed but it's failover is temporarily disabled.
            if (agent == null) {
                continue;
            }
            final ExactZonesConfig agentZones = agent.getExactZones();
            if (!key.gridServiceAgentZones.equals(agentZones)) {
                // the key.agentZones is different than agentZones
                // move allocation from key.agentZones to agentZones
                final CapacityRequirements capacity = allocatedCapacityPerAgent.getAgentCapacity(agentUid);
                markCapacityForDeallocation(key, agentUid, capacity);
                deallocateCapacity(key, agentUid, capacity);
                final StateKey newKey = new StateKey(key.pu, agentZones);
                setMachineIsolation(newKey, getMachineIsolation(key));
                allocateCapacity(newKey, agentUid, capacity);
                changed = true;
            }
        }
        if (getState(key).allocatedCapacity.equalsZero()) {
            removeKey(key);
        }
        return changed;
    }

    private void removeKey(StateKey key) {
        if (!getState(key).equalsZero()) {
            throw new IllegalStateException("Cannot remove " + key + " since it does not equal zero " + getState(key));
        }
        state.remove(key);
    }

    public RecoveringFailedGridServiceAgent[] getAgentsMarkedAsFailedNotBeingRecovered(StateKey key) {
        final Set<String> restartingAgentUids = new LinkedHashSet<String>();
        for (GridServiceAgentFutures futureAgents : getFutureAgents(key)) {
            for (FutureGridServiceAgent futureAgent: futureAgents.getFutureGridServiceAgents() ) {
                final FailedGridServiceAgent failedAgent = futureAgent.getFailedGridServiceAgent();
                if (failedAgent != null) {
                    restartingAgentUids.add(failedAgent.getAgentUid());
                }
            }
        }
        
        final List<RecoveringFailedGridServiceAgent> failedAgentsForKey = new ArrayList<RecoveringFailedGridServiceAgent>();
        for (RecoveringFailedGridServiceAgent failedAgent : getState(key).getFailedAgents()) {
            if (!restartingAgentUids.contains(failedAgent.getAgentUid())) {
                failedAgentsForKey.add(failedAgent);
            }
        }
        return failedAgentsForKey.toArray(new RecoveringFailedGridServiceAgent[failedAgentsForKey.size()]);
    }

	public RecoveringFailedGridServiceAgent[] getAgentsMarkedAsFailed(StateKey key) {
		Collection<RecoveringFailedGridServiceAgent> failedAgentsForKey = getState(key).getFailedAgents();
		return failedAgentsForKey.toArray(new RecoveringFailedGridServiceAgent[failedAgentsForKey.size()]);
	}

	private void addFailedAgent(StateKey key, String agentUid) {
		RecoveringFailedGridServiceAgent failedAgent = null;
		for (final StateValue value : state.values()) {
			for (final RecoveringFailedGridServiceAgent otherFailedAgent : value.getFailedAgents()) {
				if (otherFailedAgent.getAgentUid().equals(agentUid)) {
					// this is not the first time we detected this agent failed
					// another PU/key detected it first.
					failedAgent = otherFailedAgent;
					break;
				}
			}
		}
		if (failedAgent == null) {
			//this is the first time we detected this agent failed
			failedAgent = new RecoveringFailedGridServiceAgent(agentUid);
		}
		getState(key).addFailedAgent(failedAgent);
	}
	
    public void beforeUndeployProcessingUnit(ProcessingUnit pu) {
        validatedUndeployNotInProgressPerProcessingUnit.remove(pu);
    }
    
    /**
     * Removes all state related to the specified processing unit
     * Call this method only if you are not going to call any other state method on this pu
     */
    public void afterUndeployProcessingUnit(ProcessingUnit pu) {
        Iterator<StateKey> stateKeyIterator = state.keySet().iterator();
        while(stateKeyIterator.hasNext()) {
            ProcessingUnit statePu = stateKeyIterator.next().pu; 
            if (statePu.equals(pu)) {
                stateKeyIterator.remove();
            }
        }
        recoveredStatePerProcessingUnit.remove(pu);
        cloudCleanupPerProcessingUnit.remove(pu);
    }

    public Map<StateKey, StateValue> getStateForProcessingUnit(ProcessingUnit pu) {
        //treemap is needed for deterministic toString  
        Map<StateKey, StateValue> pustate = new TreeMap<StateKey,StateValue>();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            if (pair.getKey().pu.equals(pu)) {
                if (!pair.getValue().equalsZero()) {
                    pustate.put(pair.getKey(), pair.getValue());
                }
            }
        }
        return pustate;
    }

    public void validateUndeployNotInProgress(ProcessingUnit pu) throws UndeployInProgressException {
        
        if (!validatedUndeployNotInProgressPerProcessingUnit.contains(pu)) {
            
            // undeploy of processing unit is in process somewhere else

            Map<StateKey, StateValue> filteredState = getStateForProcessingUnit(pu);
            if (!filteredState.isEmpty()) {
                UndeployInProgressException undeployInProgressException = new UndeployInProgressException(pu);
                logger.info(undeployInProgressException.getMessage() + " Details: "+ filteredState.toString(), undeployInProgressException);
                throw undeployInProgressException;
            }

            // undeploy is not in progress 
            validatedUndeployNotInProgressPerProcessingUnit.add(pu);
        }
    }

    /**
     * @return true - If there are future machines in other keys that can be shared with this key
     */
    public boolean isFutureAgentsOfOtherSharedServices(StateKey key) {
        final Collection<StateKey> keysWithSameIsolation = getKeysWithSameIsolation(key);
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            StateKey otherKey = pair.getKey();
            if (!key.equals(otherKey) &&
                keysWithSameIsolation.contains(otherKey) &&
                getNumberOfFutureAgents(otherKey) > 0) {
                
                return true;
            }
        }
        return false;
    }

	public FutureCleanupCloudResources getCleanupFuture(ProcessingUnit pu) {
		return cloudCleanupPerProcessingUnit.get(pu);
	}

	public void setCleanupFuture(ProcessingUnit pu, FutureCleanupCloudResources future) {
		cloudCleanupPerProcessingUnit.put(pu, future);
	}

	/**
	 * @return true - if was previously not disabled and now is disabled
	 *         false- if already was disabled
	 */
	public String disableFailoverDetection(String ipAddress, String agentUid) {
		return agentWithFailoverDisabledPerIpAddress.put(ipAddress, agentUid);
	}
	
	public String enableFailoverDetection(String ipAddress) {
		return agentWithFailoverDisabledPerIpAddress.remove(ipAddress);
	}

	public boolean isAgentFailoverDisabled(String agentUid) {
		return agentWithFailoverDisabledPerIpAddress.values().contains(agentUid);
	}
	
	public String getAgentWithDisabledFailoverDetectionForIpAddress(String ipAddress) {
		return agentWithFailoverDisabledPerIpAddress.get(ipAddress);
	}

	public void replaceAllocation(String otherAgentUid, String newAgentUid) {
		for (StateValue puState : state.values()) {
			puState.replaceAllocation(otherAgentUid, newAgentUid);
		}
		final Object context = agentsContext.remove(otherAgentUid);
		if (context != null) {
		    addAgentContext(newAgentUid, context);
		}
	}
	
    /**
     * @return A map that can be saved into the space.
     * Note: If you change this method, change also #fromDocumentProperties and update #version properly.
     */
    public MachinesState toMachinesState() {
        
        final List<DocumentProperties> agentsProperties = new ArrayList<DocumentProperties>();
        for (StateKey key: state.keySet()) {

            for (String agentUid : getAllUsedCapacity(key).getAgentUids()) {
                final boolean isStopping = false;
                final boolean isFailed = false;
                agentsProperties.add(toAgentProperties(key, agentUid, isStopping, isFailed));
            }

            for (RecoveringFailedGridServiceAgent failedAgent : getAgentsMarkedAsFailed(key)) {
                final String agentUid = failedAgent.getAgentUid();
                final boolean isStopping = false;
                final boolean isFailed = true;
                agentsProperties.add(toAgentProperties(key, agentUid, isStopping, isFailed));
            }

            for (FutureStoppedMachine stoppingAgent : getMachinesGoingDown(key)) {
                final String agentUid = stoppingAgent.getGridServiceAgent().getUid();
                final boolean isStopping = true;
                final boolean isFailed = false;
                agentsProperties.add(toAgentProperties(key, agentUid, isStopping, isFailed));
            }
        }
        final DocumentProperties properties = new DocumentProperties()
            .setProperty("platformLogicalVersion", PlatformLogicalVersion.getLogicalVersion())
            .setProperty("agentsContext", agentsContext)
            .setProperty("agentsProperties", agentsProperties);
        
        final MachinesState machinesState = new MachinesState();
        machinesState.setProperties(properties);
        machinesState.setVersion(machinesStateVersion);
        return machinesState;
    }

    private DocumentProperties toAgentProperties(
            final StateKey key,
            final String agentUid,
            final boolean isStopping,
            final boolean isFailed) {
        final String agentZone = ZonesConfigUtils.zonesToString(key.gridServiceAgentZones);
        final String puName = key.pu.getName();
        return new DocumentProperties()
        .setProperty("puName", puName)
        .setProperty("agentZones", agentZone)
        .setProperty("agentUid", agentUid)
        .setProperty("isStopping", isStopping)
        .setProperty("isFailed", isFailed);
    }
	
	public void fromMachinesState(MachinesState state) {
		machinesStateVersion = state.getVersion();
		DocumentProperties properties = state.getProperties();
		agentsContext.clear();
		agentsContext.putAll((Map<String, Object>)properties.getProperty("agentsContext"));
		
		//detect failed agents
		final Collection<String> allUsedAgentUids = getAllUsedAgentUids();
		final Map<String, ProcessingUnit> allProcessingUnits = getAllProcessingUnits();
		final List<DocumentProperties> agentsProperties = properties.getProperty("agentsProperties");
		for (DocumentProperties agentProperties : agentsProperties) {
		    final boolean isStopping =  (Boolean)agentProperties.getProperty("isStopping");
		    final String agentUid =  agentProperties.getProperty("agentUid");
		    final String puName = agentProperties.getProperty("puName");
		    final ProcessingUnit pu = allProcessingUnits.get(puName);
		    boolean isFailed =  (Boolean)agentProperties.getProperty("isFailed");
		    if (pu == null) {
                logger.info("Ignoring missing " + puName + " agent " + agentUid + " since " + puName + " was uninstalled");
                isFailed = false;
            }
		    else if (!allUsedAgentUids.contains(agentUid)) {
		        
		        if (isStopping) {
                    logger.info("Ignoring missing " + puName + " agent " + agentUid + " since it was being stopped");
                }
		        else if (isFailed) {
                    logger.info("Marking " + puName + " agent " + agentUid + " as failed since it was previously marked as failed.");
		        }
		        else {
		            // Agent probably failed while ESM was restarting 
		            logger.info("Marking " + puName + " agent " + agentUid + " as failed since it cannot be discovered.");
		            isFailed = true;
		        }
		    }

		    if (isFailed) {
		        final ZonesConfig agentZones = ZonesConfigUtils.zonesFromString((String)agentProperties.getProperty("agentZones"));
		        final StateKey key = new StateKey(pu, agentZones);
		        addFailedAgent(key, agentUid);
		    }
		}
	}

    private Map<String, ProcessingUnit> getAllProcessingUnits() {
        final Map<String, ProcessingUnit> puNames = new LinkedHashMap<String, ProcessingUnit>();
        for (StateKey key : this.state.keySet()) {
           final ProcessingUnit pu = key.pu;
           puNames.put(pu.getName(), pu);
        }
        return puNames;
    }

    public long getVersion() {
        return machinesStateVersion;
    }
}
