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
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState.StateKey;
import org.openspaces.grid.gsm.machines.exceptions.CannotDetermineIfNeedToStartMoreMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.DelayingScaleInUntilAllMachinesHaveStartedException;
import org.openspaces.grid.gsm.machines.exceptions.FailedToDiscoverMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.FailedToStartNewMachineException;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementPendingContainerDeallocationException;
import org.openspaces.grid.gsm.machines.exceptions.InconsistentMachineProvisioningException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToStartMoreGridServiceAgentsException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.StartedTooManyMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.UnexpectedShutdownOfNewGridServiceAgentException;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioningException;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;

/**
 * This class tracks started and shutdown machines while the operating is in progress. 
 * It uses internal logic and the bin packing solver to allocated and deallocate capacity (cpu/memory) on these machines.
 * When there is excess capacity, machines are marked for deallocation. 
 * When there is capacity shortage, new machines are started.
 * 
 * @author itaif
 * @see MachinesSlaEnforcement - creates this endpoint
 * @see MachinesSlaPolicy - defines the sla policy for this endpoint
 */
class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint {

    private static final int START_AGENT_TIMEOUT_SECONDS = 30*60;
    private static final long STOP_AGENT_TIMEOUT_SECONDS = 10*60;

    private final ProcessingUnit pu;
    private final Log logger;
    private final MachinesSlaEnforcementState state;
    
    public DefaultMachinesSlaEnforcementEndpoint(ProcessingUnit pu, MachinesSlaEnforcementState state) {
        
        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null.");
        }
        this.state = state;
        this.pu = pu;           
        this.logger = 
            new LogPerProcessingUnit(
                new SingleThreadedPollingLog( 
                        LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class)),
                pu);
    }

    @Override
    public CapacityRequirementsPerAgent getAllocatedCapacity(AbstractMachinesSlaPolicy sla) {
        CapacityRequirementsPerAgent allocatedCapacity = getAllocatedCapacityUnfiltered(sla);
        // validate all agents have been discovered
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
            if (agent == null) {
                throw new IllegalStateException("agent " + agentUid +" is not discovered");
            }
        }
        return allocatedCapacity;
     }

    public CapacityRequirementsPerAgent getAllocatedCapacityUnfiltered(AbstractMachinesSlaPolicy sla) {
       return state.getAllocatedCapacity(getKey(sla));
    }

    @Override
    public CapacityRequirementsPerAgent getAllocatedCapacityFilterUndiscoveredAgents(AbstractMachinesSlaPolicy sla) {
        CapacityRequirementsPerAgent checkedAllocatedCapacity = new CapacityRequirementsPerAgent(); 
        CapacityRequirementsPerAgent allocatedCapacity = getAllocatedCapacityUnfiltered(sla);
        // validate all agents have been discovered
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
            if (agent != null) {
                CapacityRequirements capacity = allocatedCapacity.getAgentCapacity(agentUid);
                checkedAllocatedCapacity = checkedAllocatedCapacity.add(agentUid,capacity);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found llocated capacity on agent that is no longer discovered " + agentUid);
                }
            }
        }
        return checkedAllocatedCapacity;
     }
    
    public void enforceSla(CapacityMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException {
        
        validateSla(sla);
        
        long memoryInMB = MachinesSlaUtils.getMemoryInMB(sla.getCapacityRequirements());
        if (memoryInMB < 
                sla.getMinimumNumberOfMachines()*sla.getContainerMemoryCapacityInMB()) {
            throw new IllegalArgumentException(
                    "Memory capacity " + memoryInMB + "MB "+
                    "is less than the minimum of " + sla.getMinimumNumberOfMachines() + " "+
                    "containers with " + sla.getContainerMemoryCapacityInMB() + "MB each.");
        }
        
        if (memoryInMB > sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB()) {
            throw new IllegalArgumentException(
                    "Memory capacity " + memoryInMB + "MB "+
                    "is more than the maximum of " + sla.getMaximumNumberOfMachines() + " "+
                    "containers with " + sla.getContainerMemoryCapacityInMB() + "MB each.");
        }
    
        validateProvisionedMachines(sla);
        setMachineIsolation(sla);
        enforceSlaInternal(sla);
            
    }

    private StateKey getKey(AbstractMachinesSlaPolicy sla) {
        return new MachinesSlaEnforcementState.StateKey(pu, sla.getGridServiceAgentZones());
    }

    @Override
    public void recoverStateOnEsmStart(AbstractMachinesSlaPolicy sla) throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException {
    
        if (!isCompletedStateRecovery(sla)) {
            
            setMachineIsolation(sla);
            
            Set<String> puZones = pu.getRequiredContainerZones().getZones();
            
            // check pu zone matches container zones.
            if (puZones.size() != 1) {
                throw new IllegalStateException("PU has to have exactly 1 zone defined");
            }
    
            String containerZone = puZones.iterator().next();
            Admin admin = pu.getAdmin();

            // Validate all Agents have been discovered.
            for (ProcessingUnitInstance instance : pu.getInstances()) {
                GridServiceContainer container = instance.getGridServiceContainer();
                if (container.getAgentId() != -1 && container.getGridServiceAgent() == null) {
                    throw new NeedToWaitUntilAllGridServiceAgentsDiscoveredException(pu, container);
                }
            }
        
            // Recover the endpoint state based on running containers.
            CapacityRequirementsPerAgent allocatedCapacityForPu = state.getAllocatedCapacity(pu);
            for (GridServiceAgent agent: admin.getGridServiceAgents()) {
                if (!sla.getGridServiceAgentZones().isSatisfiedBy(agent.getExactZones())) {
                    continue;
                }
                String agentUid = agent.getUid();
                
                // state maps [agentUid,PU] into memory capacity
                // we cannot assume allocatedMemoryOnAgent == 0 since this method
                // must be idempotent.
                long allocatedMemoryOnAgentInMB = MachinesSlaUtils.getMemoryInMB(
                      allocatedCapacityForPu.getAgentCapacityOrZero(agentUid));
                
                int numberOfContainersForPuOnAgent = 
                        ContainersSlaUtils.getContainersByZoneOnAgentUid(admin,containerZone,agentUid).size();
                
                long memoryToAllocateOnAgentInMB = 
                        numberOfContainersForPuOnAgent * sla.getContainerMemoryCapacityInMB() - allocatedMemoryOnAgentInMB;

                if (memoryToAllocateOnAgentInMB > 0) {
                    logger.info("Recovering " + memoryToAllocateOnAgentInMB + "MB allocated for PU" + pu.getName() + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine()));
                    CapacityRequirements capacityToAllocateOnAgent = 
                            new CapacityRequirements(new MemoryCapacityRequirement(memoryToAllocateOnAgentInMB));
                    
                    allocateManualCapacity(
                            sla,
                            capacityToAllocateOnAgent, 
                            new CapacityRequirementsPerAgent().add(
                                    agentUid, 
                                    capacityToAllocateOnAgent));
                }
            }
    
            completedStateRecovery(sla);
        }
    }

    /**
     * Validates that the cloud has not "forgot about" machines without killing the agent on them.
     * This can happen when the connection with the cloud is lost and we cannot determine which machines are allocated for this pu by the cloud.
     * 
     * We added this method since in such condition the code behaves unexpectedly (all machines are unallocated automatically, and then some other pu tries to allocate them once the cloud is back online).
     * @param sla
     * @throws InconsistentMachineProvisioningException 
     * @throws FailedToDiscoverMachinesException 
     * @throws WaitingForDiscoveredMachinesException 
     */
    private void validateProvisionedMachines(AbstractMachinesSlaPolicy sla) throws GridServiceAgentSlaEnforcementInProgressException, MachinesSlaEnforcementInProgressException  {

        Collection<GridServiceAgent> discoveredAgents =sla.getDiscoveredMachinesCache().getDiscoveredAgents();
        Collection<GridServiceAgent> undiscoveredAgents = new HashSet<GridServiceAgent>();
        for(GridServiceAgent agent : MachinesSlaUtils.convertAgentUidsToAgentsIfDiscovered(getAllocatedCapacityUnfiltered(sla).getAgentUids(),pu.getAdmin())) {
           
            if (!discoveredAgents.contains(agent)) {
                undiscoveredAgents.add(agent);
           }
        }
        if (undiscoveredAgents.size() > 0) {
            throw new InconsistentMachineProvisioningException(new String[]{getProcessingUnit().getName()}, undiscoveredAgents);
        }
    }

    private void validateSla(AbstractMachinesSlaPolicy sla) {
        
        if (sla == null) {
            throw new IllegalArgumentException("SLA cannot be null");
        }
        
        sla.validate();
    }
    
    @Override
    public void enforceSla(EagerMachinesSlaPolicy sla)
            throws GridServiceAgentSlaEnforcementInProgressException {
        
        validateSla(sla);
        
        try {
            validateProvisionedMachines(sla);
        } catch (MachinesSlaEnforcementInProgressException e) {
            logger.warn("Ignoring failure to related to new machines, since now in eager mode", e);
        }
        
        setMachineIsolation(sla);
        enforceSlaInternal(sla);
    }

    private void enforceSlaInternal(EagerMachinesSlaPolicy sla) 
            throws GridServiceAgentSlaEnforcementInProgressException {
        
        try {
            updateFutureAgentsState(sla);

            updateFailedAndUnprovisionedMachinesState(sla);
            updateAgentsMarkedForDeallocationState(sla);
            
            unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(sla);
        
            //Eager scale out: allocate as many machines and as many CPU as possible
            allocateEagerCapacity(sla);
    
        } catch (MachinesSlaEnforcementInProgressException e) {
            logger.warn("Ignoring failure to related to new machines, since now in eager mode", e);
        }
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        if (machineShortage > 0) {
            CapacityRequirements capacityRequirements = new CapacityRequirements(
                    new NumberOfMachinesCapacityRequirement(machineShortage));
            throw new NeedToStartMoreGridServiceAgentsException(sla, state, capacityRequirements, pu);
        }
        
        if (!getCapacityMarkedForDeallocation(sla).equalsZero()) {
            // containers need to be removed (required when number of containers per machine changes)
            throw new GridServiceAgentSlaEnforcementPendingContainerDeallocationException(new String[]{getProcessingUnit().getName()}, getCapacityMarkedForDeallocation(sla));
        }
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }
            
    private void enforceSlaInternal(CapacityMachinesSlaPolicy sla)
            throws MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException {

        updateFutureAgentsState(sla);
        updateFailedAndUnprovisionedMachinesState(sla);
        updateAgentsMarkedForDeallocationState(sla);
        
        CapacityRequirementsPerAgent capacityMarkedForDeallocation = getCapacityMarkedForDeallocation(sla);
        CapacityRequirementsPerAgent capacityAllocated = getAllocatedCapacity(sla);
        
        if (getNumberOfFutureAgents(sla) > 0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending deallocation.");
        }
        
        CapacityRequirements target = sla.getCapacityRequirements();
            
        CapacityRequirementsPerAgent capacityAllocatedAndMarked = 
            capacityMarkedForDeallocation.add(capacityAllocated);
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        
        if (!capacityAllocatedAndMarked.getTotalAllocatedCapacity().equals(target) &&
            capacityAllocatedAndMarked.getTotalAllocatedCapacity().greaterOrEquals(target) &&
            machineShortage == 0) {
            
            if (getNumberOfFutureAgents(sla) > 0) {
                throw new DelayingScaleInUntilAllMachinesHaveStartedException(new String[]{getProcessingUnit().getName()});
            }
            
            logger.debug("Considering scale in: "+
                    "target is "+ target + " " +
                    "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                    "machines started " + getAllocatedCapacity(sla) + ", " + 
                    "machines pending deallocation " + getCapacityMarkedForDeallocation(sla));
            
            // scale in
            CapacityRequirements surplusCapacity = 
                capacityAllocatedAndMarked.getTotalAllocatedCapacity().subtract(target);           
            int surplusMachines = capacityAllocatedAndMarked.getAgentUids().size() - sla.getMinimumNumberOfMachines();
            
            // adjust surplusMemory based on agents marked for deallocation
            // remove mark if it would cause surplus to be below zero
            // remove mark if it would reduce the number of machines below the sla minimum.
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {

                CapacityRequirements agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                if (surplusCapacity.greaterOrEquals(agentCapacity) &&
                    surplusMachines > 0) {
                    // this machine is already marked for deallocation, so surplus
                    // is adjusted to reflect that
                    surplusCapacity = surplusCapacity.subtract(agentCapacity);
                    surplusMachines--;
                } else {
                    // cancel scale in
                    unmarkCapacityForDeallocation(sla, agentUid, agentCapacity);
                    if (logger.isInfoEnabled()) {
                        GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                        if (agent != null) {
                        logger.info(
                                "machine agent " + agent.getMachine().getHostAddress() + " " +
                                "is no longer marked for deallocation in order to maintain capacity. "+
                                "Allocated machine agents are: " + getAllocatedCapacity(sla));
                        }
                    }
                }
            }

            if (!surplusCapacity.equalsZero()) {
                // scale in now
                deallocateManualCapacity(sla,surplusCapacity);
            }
        }
        
        else if (!capacityAllocatedAndMarked.getTotalAllocatedCapacity().greaterOrEquals(target)) {
            
            // scale out

            if (logger.isInfoEnabled()) {
                logger.info("Considering to start more machines inorder to reach target capacity of " + target +". "+
                            "Current capacity is " + getAllocatedCapacity(sla).getTotalAllocatedCapacity()); 
            }
            CapacityRequirements shortageCapacity = getCapacityShortage(sla, target);
            
            // unmark all machines pending deallocation              
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                if (MachinesSlaUtils.getMemoryInMB(shortageCapacity) == 0L) {
                    break;
                }
            	CapacityRequirements agentCapacity =  capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                CapacityRequirements requiredCapacity = agentCapacity.min(shortageCapacity);
                if (logger.isInfoEnabled()) {
                    GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                    if (agent != null) {
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for deallocation in order to maintain capacity.");
                    }
                }
                unmarkCapacityForDeallocation(sla, agentUid, requiredCapacity);
                shortageCapacity = shortageCapacity.subtract(requiredCapacity);
            }
            
           if (!shortageCapacity.equalsZero()) {
               allocateManualCapacity(sla, shortageCapacity);
               shortageCapacity = getCapacityShortage(sla, target);               
           }
           
           if (!shortageCapacity.equalsZero()) {
               if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                   throw new NeedToStartMoreGridServiceAgentsException(sla, state,shortageCapacity,pu);
               }
               
                ExactZonesConfig exactZones = new ExactZonesConfigurer().addZones(sla.getGridServiceAgentZones().getZones()).create();
                FutureGridServiceAgent[] futureAgents = sla.getMachineProvisioning().startMachinesAsync(
                    shortageCapacity, 
                    exactZones,
                    START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                addFutureAgents(sla, futureAgents, shortageCapacity);
                
                logger.info(
                        "One or more new machine(s) is started in order to "+
                        "fill capacity shortage " + shortageCapacity + " " + 
                        "for zones " + exactZones +" "+
                        "Allocated machine agents are: " + getAllocatedCapacity(sla) +" "+
                        "Pending future machine(s) requests " + getNumberOfFutureAgents(sla));
                
           }
            
        }
        // we check minimum number of machines last since it was likely dealt with 
        // by enforcing the capacity requirements.
        else if (machineShortage > 0) {
            
            logger.info("Considering to start more machines to reach required minimum number of machines: " + 
                    capacityAllocated + " started, " +
                    capacityMarkedForDeallocation + " marked for deallocation, " +
                    sla.getMinimumNumberOfMachines() + " is the required minimum number of machines."
            );
            
               
            machineShortage = unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(sla);
            
            
            if (machineShortage > 0) {
                //try allocate on new machines, that have other PUs on it.
                allocateNumberOfMachines(sla, machineShortage);
                machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
            }
            
            if (machineShortage > 0) {
                
                // scale out to get to the minimum number of agents
                CapacityRequirements capacityRequirements = new CapacityRequirements(
                        new NumberOfMachinesCapacityRequirement(machineShortage));
                
                if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                    throw new NeedToStartMoreGridServiceAgentsException(sla, state, capacityRequirements, pu);
                }
                
                ExactZonesConfig exactZones = new ExactZonesConfigurer().addZones(sla.getGridServiceAgentZones().getZones()).create();
                FutureGridServiceAgent[] futureAgents = sla.getMachineProvisioning().startMachinesAsync(
                        capacityRequirements,
                        exactZones,
                        START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                addFutureAgents(sla, futureAgents, capacityRequirements);
                
                logger.info(
                        machineShortage+ " new machine(s) is scheduled to be started in order to reach the minimum of " + 
                        sla.getMinimumNumberOfMachines() + " machines, for zones " + exactZones + ". " +
                        "Allocated machine agents are: " + getAllocatedCapacity(sla));
                
            }
            
            // even if machineShortage is 0, we still need to come back to this method 
            // to check the capacity is satisfied (scale out)
            throw new MachinesSlaEnforcementInProgressException(new String[] {getProcessingUnit().getName()});
        }
        else {
            logger.debug("No action required in order to enforce machines sla. "+
                    "target="+target + "| " + 
                    "allocated="+capacityAllocated.toDetailedString() + "| " +
                    "marked for deallocation="+capacityMarkedForDeallocation.toDetailedString() + "| " +
                    "#futures="+getNumberOfFutureAgents(sla) + " |" +
                    "#minimumMachines="+sla.getMinimumNumberOfMachines());        
        }
        

        if (!getCapacityMarkedForDeallocation(sla).equalsZero()) {
            // containers need to move to another machine
            throw new GridServiceAgentSlaEnforcementPendingContainerDeallocationException(new String[]{getProcessingUnit().getName()}, getCapacityMarkedForDeallocation(sla));
        }
        
        if (getNumberOfFutureAgents(sla) > 0) {
            // new machines need to be started
            throw new MachinesSlaEnforcementInProgressException(new String[] {getProcessingUnit().getName()});
        }
        
        if (!getAgentUidsGoingDown(sla).isEmpty()) {
            // old machines need to complete shutdown
            throw new MachinesSlaEnforcementInProgressException(new String[] {getProcessingUnit().getName()});
        }
    }

    private CapacityRequirements getCapacityShortage(CapacityMachinesSlaPolicy sla, CapacityRequirements target) throws MachinesSlaEnforcementInProgressException {
        CapacityRequirements shortageCapacity = 
            target.subtractOrZero(getAllocatedCapacity(sla).getTotalAllocatedCapacity());
        
        // take into account expected machines into shortage calculate
        for (GridServiceAgentFutures futureAgents : getFutureAgents(sla)) {
                            
            CapacityRequirements expectedCapacityRequirements = futureAgents.getExpectedCapacity(); 
            for (CapacityRequirement shortageCapacityRequirement : shortageCapacity.getRequirements()) {
               
                CapacityRequirement expectedCapacityRequirement = expectedCapacityRequirements.getRequirement(shortageCapacityRequirement.getType());
                if (!shortageCapacityRequirement.equalsZero() && expectedCapacityRequirement.equalsZero()) {
                    // cannot determine expected capacity, it could be enough to satisfy shortage 
                    // and if that is the case, there is no point in declaring there is shortage.
                    throw new MachinesSlaEnforcementInProgressException(new String[] {getProcessingUnit().getName()});
                }
            }
            
            shortageCapacity = 
                shortageCapacity.subtractOrZero(expectedCapacityRequirements);
        }
        return shortageCapacity;
    }

    /**
     * if minimum number of machines is breached then
     * unmark machines that have only containers that are pending for deallocation
     * @throws CannotDetermineIfNeedToStartMoreMachinesException 
     */
    private int unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla) throws CannotDetermineIfNeedToStartMoreMachinesException {
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        
        final CapacityRequirementsPerAgent capacityAllocated = getAllocatedCapacity(sla);
        CapacityRequirementsPerAgent capacityMarkedForDeallocation = getCapacityMarkedForDeallocation(sla);

        if (!capacityMarkedForDeallocation.equalsZero()) {
            
            // unmark machines that have only containers that are pending deallocation
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                if (machineShortage > 0 && !capacityAllocated.getAgentUids().contains(agentUid)) {
                
                    CapacityRequirements agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                    unmarkCapacityForDeallocation(sla, agentUid, agentCapacity);
                    machineShortage--;
                
                    if (logger.isInfoEnabled()) {
                        GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                        if (agent != null) {
                            logger.info(
                                    "machine " + MachinesSlaUtils.machineToString(agent.getMachine()) + " "+
                                    "is no longer marked for deallocation in order to reach the minimum of " + 
                                    sla.getMinimumNumberOfMachines() + " machines.");
                        }
                    }
                }
            }
        }
        return machineShortage;
    }

    /**
     * @return minimumNumberOfMachines - allocatedMachines - futureMachines
     * @throws CannotDetermineIfNeedToStartMoreMachinesException 
     */
    private int getMachineShortageInOrderToReachMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla) throws CannotDetermineIfNeedToStartMoreMachinesException
            {
        
        boolean cannotDetermineExpectedNumberOfMachines = false;
        final CapacityRequirementsPerAgent capacityAllocated = getAllocatedCapacity(sla);
        int machineShortage = sla.getMinimumNumberOfMachines() - capacityAllocated.getAgentUids().size();
        if (getNumberOfFutureAgents(sla) > 0) {
            // take into account expected machines into shortage calculate
            for (final GridServiceAgentFutures future : getFutureAgents(sla)) {
                
                final int expectedNumberOfMachines = numberOfMachines(future.getExpectedCapacity());
                if (expectedNumberOfMachines == 0) {
                    cannotDetermineExpectedNumberOfMachines = true;
                }
                else {
                    machineShortage -= expectedNumberOfMachines;
                }
            }
        }
        
        if (machineShortage > 0 && cannotDetermineExpectedNumberOfMachines) {
            throw new CannotDetermineIfNeedToStartMoreMachinesException(new String[]{getProcessingUnit().getName()}, machineShortage);
        }
        
        if (machineShortage < 0) {
            machineShortage = 0;
        }
        
        return machineShortage;
    }

    /**
     * Kill agents marked for deallocation that no longer manage containers. 
     * @param sla 
     * @param machineProvisioning
     */
    private void updateAgentsMarkedForDeallocationState(AbstractMachinesSlaPolicy sla) {
        CapacityRequirementsPerAgent capacityMarkedForDeallocation = getCapacityMarkedForDeallocation(sla);
        for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
            
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);

            if (agent == null) {
                deallocateAgentCapacity(sla, agentUid);
                logger.info("pu " + pu.getName() + " agent " + agentUid + " has shutdown.");
            }
            else if (MachinesSlaUtils.getMemoryInMB(getAllocatedCapacity(sla).getAgentCapacityOrZero(agentUid))>=sla.getContainerMemoryCapacityInMB()) {
                deallocateAgentCapacity(sla, agentUid);
                logger.info("pu " + pu.getName() +" is still allocated on agent " + agentUid);
            }
            else if (MachinesSlaUtils.getNumberOfChildContainersForProcessingUnit(agent,pu) == 0) {
                    
                if (!sla.isStopMachineSupported()) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since scale strategy " + sla.getScaleStrategyName()
                            + " does not support automatic start/stop of machines");
                    deallocateAgentCapacity(sla, agentUid);
                    continue;
                }
                
                if (!MachinesSlaUtils.isAgentAutoShutdownEnabled(agent)) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it does not have the auto-shutdown flag");
                    deallocateAgentCapacity(sla, agentUid);
                    continue;
                }
                
                if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it is running management processes.");
                    deallocateAgentCapacity(sla, agentUid);
                    continue;
                }
                
                if (state.isAgentSharedWithOtherProcessingUnits(pu,agent.getUid())) {
                 // This is a bit like reference counting, the last pu to leave stops the machine
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it is shared with other processing units.");
                    deallocateAgentCapacity(sla, agentUid);
                    continue;
                }
                
            
                // double check that agent is not used
                Set<Long> childProcessesIds = MachinesSlaUtils.getChildProcessesIds(agent);
                if (!childProcessesIds.isEmpty()) {
                    // this is unexpected. We already checked for management processes and other PU processes.
                    logger.warn("Agent " + agent.getUid() + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " cannot be shutdown due to the following child processes: " + childProcessesIds);
                    continue;
                }
                
                logger.info(
                       "Agent " + agent.getUid() + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " is no longer in use by any processing unit. "+
                       "It is going down!");

               stopMachine(sla, agent);
            }
        }
        
        cleanMachinesGoingDown(sla);
    }

    private void stopMachine(AbstractMachinesSlaPolicy sla, GridServiceAgent agent) {
        final NonBlockingElasticMachineProvisioning machineProvisioning = sla.getMachineProvisioning();
        // The machineProvisioning might not be the same one that started this agent,
        // Nevertheless, we expect it to be able to stop this agent.
        machineProvisioning.stopMachineAsync(
                agent, 
                STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        agentGoingDown(sla, agent);
    }

    /**
     * mark for deallocation:
     * failed machines ( not in lookup service )
     * machines that are no longer in sla.getProvisionedMacines()
     * machines that were not restricted but are now restricted for this PU
     */
    private void updateFailedAndUnprovisionedMachinesState(AbstractMachinesSlaPolicy sla) {
        
        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();
        
        // mark for deallocation all failed machines
        for(String agentUid: getAllocatedCapacityUnfiltered(sla).getAgentUids()) {
            if (agents.getAgentByUID(agentUid) == null) {
                logger.warn("Agent " + agentUid + " was killed unexpectedly.");
                markAgentCapacityForDeallocation(sla, agentUid);
            }
        }
        
        // mark for deallocation machines that are restricted for this PU
        Map<String,List<String>> restrictedMachines = getRestrictedAgentUidsForPuWithReason(sla);
        for(GridServiceAgent agent : MachinesSlaUtils.convertAgentUidsToAgents(getAllocatedCapacity(sla).getAgentUids(),pu.getAdmin())) {
            String agentUid = agent.getUid();
            if (restrictedMachines.containsKey(agentUid)) {
                logger.info("Machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " is restricted for pu " + pu.getName() + " reason:" + restrictedMachines.get(agentUid));
                markAgentCapacityForDeallocation(sla, agentUid);
            }
        }
    }
    
    private void cleanMachinesGoingDown(AbstractMachinesSlaPolicy sla) {

        final NonBlockingElasticMachineProvisioning machineProvisioning = sla.getMachineProvisioning();
        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();

        // cleanup agents that were gracefully shutdown
        for (String agentUid: getAgentUidsGoingDown(sla)) {
            GridServiceAgent agent = agents.getAgentByUID(agentUid);
            if (agent == null) {
                logger.warn("Agent " + agentUid + " shutdown completed succesfully.");
                state.agentShutdownComplete(agentUid);
            }
            
            else if (state.isAgentUidGoingDownTimedOut(agentUid)) {
                
                logger.warn("Failed to shutdown agent " + agentUid + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine()) +" . Retrying one last time.");
                
                machineProvisioning.stopMachineAsync(
                        agent, 
                        STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                // no more retries
                state.agentShutdownComplete(agentUid);
            }
        }

    }
    
    /**
     * Move future agents that completed startup, from the future state to allocated state. 
     * 
     * This is done by removing the future state, and adding pu allocation state on these machines.
     * @throws InconsistentMachineProvisioningException 
     * @throws UnexpectedShutdownOfNewGridServiceAgentException 
     * @throws FailedToStartNewMachineException 
     * @throws StartedTooManyMachinesException 
     * @throws FailedToDiscoverMachinesException 
     * @throws WaitingForDiscoveredMachinesException 
     */
    private void updateFutureAgentsState(AbstractMachinesSlaPolicy sla) throws GridServiceAgentSlaEnforcementInProgressException, MachinesSlaEnforcementInProgressException  {
       
        // each futureAgents object contains an array of FutureGridServiceAgent
        // only when all future in the array is done, the futureAgents object is done.
        Collection<GridServiceAgentFutures> doneFutureAgentss = getAllDoneFutureAgents(sla);
        Collection<GridServiceAgent> discoveredAgents = sla.getDiscoveredMachinesCache().getDiscoveredAgents();
        
        for (GridServiceAgentFutures doneFutureAgents : doneFutureAgentss) {
            
            for (FutureGridServiceAgent doneFutureAgent : doneFutureAgents.getFutureGridServiceAgents()) {
                try {
                    validateHealthyAgent(
                            sla,
                            discoveredAgents,
                            doneFutureAgent);
                }
                catch (InconsistentMachineProvisioningException e) {
                    // should be fixed by next time we get here
                    // do not remove the future
                    throw e;
                } catch (FailedToStartNewMachineException e) {
                    // we remove the future, next time we wont find it
                    doneFutureAgents.removeFutureAgent(doneFutureAgent);
                    if (sla.isUndeploying()) {
                        logger.info("Ignoring failure to start new machine, since undeploy is in progress",e);
                    }
                    else {
                        throw e;
                    }
                } catch (UnexpectedShutdownOfNewGridServiceAgentException e) {
                    // we remove the future, next time we wont find it
                    doneFutureAgents.removeFutureAgent(doneFutureAgent);
                    if (sla.isUndeploying()) {
                        logger.info("Ignoring failure to start new grid service agent, since undeploy is in progress",e);
                    }
                    else {
                        throw e;
                    }
                }
            }
        }
        
        for (GridServiceAgentFutures doneFutureAgents : doneFutureAgentss) {
       
            Collection<GridServiceAgent> healthyAgents = doneFutureAgents.getGridServiceAgents();
            
            if (healthyAgents.size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Agents that were started by " + 
                            "machineProvisioning class=" + sla.getMachineProvisioning().getClass() + " " +
                            "new agents: " + MachinesSlaUtils.machinesToString(healthyAgents) + " " +
                            "provisioned agents:" + MachinesSlaUtils.machinesToString(discoveredAgents));
                }
                CapacityRequirementsPerAgent unallocatedCapacity = 
                    getUnallocatedCapacityIncludeNewMachines(sla, healthyAgents);
                            
                //update state 1: allocate capacity based on expectation (even if it was not met)
                if (numberOfMachines(doneFutureAgents.getExpectedCapacity()) > 0) {
                    allocateNumberOfMachines(
                            sla, 
                            numberOfMachines(doneFutureAgents.getExpectedCapacity()), 
                            unallocatedCapacity);
                }
                else if (!doneFutureAgents.getExpectedCapacity().equalsZero()) {
                    allocateManualCapacity(
                            sla, 
                            doneFutureAgents.getExpectedCapacity(), 
                            unallocatedCapacity); 
                }
                else {
                    throw new IllegalStateException("futureAgents expected capacity malformed. doneFutureAgents=" + 
                            doneFutureAgents.getExpectedCapacity());
                }
            }
            
            //update state 2: remove futures from list
            removeFutureAgents(sla, doneFutureAgents);
            
            // update state 3:
            // mark for shutdown new machines that are not marked by any processing unit
            // these are all future agents that should have been used by the binpacking solver. 
            // The fact that they have not been used suggests that the number of started 
            // machines by machineProvisioning is more than we asked for.
            Collection<GridServiceAgent> unallocatedAgents = new ArrayList<GridServiceAgent>();
            
            for (GridServiceAgent newAgent : healthyAgents) {
                String agentUid = newAgent.getUid();
                CapacityRequirements agentCapacity = getAllocatedCapacity(sla).getAgentCapacityOrZero(agentUid);
                if (agentCapacity.equalsZero()) {
                    unallocatedAgents.add(newAgent);
                }
            }
            
            if (unallocatedAgents.size() > 0) {
                if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                    logger.info(
                            "Agents " + MachinesSlaUtils.machinesToString(unallocatedAgents) +
                             " are not needed for pu " + pu.getName());
                }
                else {
                    StartedTooManyMachinesException tooManyMachinesExceptions = new StartedTooManyMachinesException(pu,unallocatedAgents);
                    logger.warn(
                            "Stopping machines " + MachinesSlaUtils.machinesToString(unallocatedAgents) + " "+
                            "Agents provisioned by cloud: " + discoveredAgents, 
                            tooManyMachinesExceptions);
                    for (GridServiceAgent unallocatedAgent : unallocatedAgents) {
                        stopMachine(sla,unallocatedAgent);
                    }
                    throw tooManyMachinesExceptions;
                }
            }
        }
        
    }

    private CapacityRequirementsPerAgent getUnallocatedCapacityIncludeNewMachines(
            AbstractMachinesSlaPolicy sla,
            Collection<GridServiceAgent> newMachines ) throws MachinesSlaEnforcementInProgressException {
        
        // unallocated capacity = unallocated capacity + new machines
        CapacityRequirementsPerAgent unallocatedCapacity = getUnallocatedCapacity(sla);
        for (GridServiceAgent newAgent : newMachines) {
            if (unallocatedCapacity.getAgentUids().contains(newAgent.getUid())) {
                throw new IllegalStateException("unallocated capacity cannot contain future agents");
            }
            
            CapacityRequirements newAgentCapacity = MachinesSlaUtils.getMachineTotalCapacity(newAgent,sla);
            if (logger.isInfoEnabled()) {
                logger.info("Agent started and provisioned succesfully on a new machine " + MachinesSlaUtils.machineToString(newAgent.getMachine())+ " has "+ newAgentCapacity);
            }
            
            if (MachinesSlaUtils.getMemoryInMB(newAgentCapacity) < sla.getContainerMemoryCapacityInMB()) {
                logger.warn("New agent " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " has only "
                        + newAgentCapacity
                        + " unreserved free resources, which is not enough for even one container that requires "
                        + sla.getContainerMemoryCapacityInMB() + "MB");
            }
            unallocatedCapacity = 
                unallocatedCapacity.add(
                    newAgent.getUid(), 
                    newAgentCapacity);
            
        }
        return unallocatedCapacity;
    }

    /**
     * @param sla 
     * @param discoveredAgents 
     * @return true - if removed future agent from the state
     * @throws InconsistentMachineProvisioningException 
     * @throws UnexpectedShutdownOfNewGridServiceAgentException 
     * @throws MachinesSlaEnforcementInProgressException 
     * @throws FailedMachineProvisioningException 
     */
    private void validateHealthyAgent(
            AbstractMachinesSlaPolicy sla, 
            Collection<GridServiceAgent> discoveredAgents, 
            FutureGridServiceAgent futureAgent) 
                    throws UnexpectedShutdownOfNewGridServiceAgentException, InconsistentMachineProvisioningException, MachinesSlaEnforcementInProgressException  {

        final NonBlockingElasticMachineProvisioning machineProvisioning = sla.getMachineProvisioning();
        final Collection<String> usedAgentUids = state.getAllUsedAgentUids();
        final Collection<String> usedAgentUidsForPu = state.getUsedAgentUids(getKey(sla));
        
        GridServiceAgent newAgent = null;
        {
            Exception exception = null;
            try {
                newAgent = futureAgent.get(); 
            } catch (ExecutionException e) {
                // if runtime or error propagate exception "as-is"
                Throwable cause = e.getCause();
                if (cause instanceof TimeoutException || cause instanceof ElasticMachineProvisioningException || cause instanceof InterruptedException) {
                    // expected exception
                    exception = e;
                }
                else {
                    throw new IllegalStateException("Unexpected Exception from machine provisioning.",e);
                }
            } catch (TimeoutException e) {
                // expected exception
                exception = e;
            }
            
            if (exception != null) {
                String[] affectedPUs = state.getProcessingUnitsOfFutureMachine(pu, futureAgent);
                throw new FailedToStartNewMachineException(affectedPUs, exception);
            }
        }
        if (newAgent == null) {
            throw new IllegalStateException("Machine provisioning future is done without exception, but returned a null agent");
        }
        
        if (!newAgent.isDiscovered()) {
            String[] affectedPUs = state.getProcessingUnitsOfFutureMachine(pu, futureAgent);
            UnexpectedShutdownOfNewGridServiceAgentException unexpectedShutdownException = new UnexpectedShutdownOfNewGridServiceAgentException(newAgent.getMachine(), affectedPUs);
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to start agent on new machine.", unexpectedShutdownException);
            }
            throw unexpectedShutdownException;
        }
        
        if (usedAgentUidsForPu.contains(newAgent.getUid())) {
            //some nasty bug in machine provisioning implementation 
            throw new IllegalStateException(
                "Machine provisioning for " + pu.getName() + " "+
                "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                " which is already in use by this PU."+
                "The machine is ignored");
        }
       
        if (usedAgentUids.contains(newAgent.getUid())) {
          //some nasty bug in machine provisioning implementation        
            throw new IllegalStateException(
                        "Machine provisioning for " + pu.getName() + " "+
                        "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                        " which is already in use by another PU."+
                        "This machine is ignored");
        }
        
        if (!discoveredAgents.contains(newAgent)) {
   
           //Handle the case of a new machine that was started with @{link {@link NonBlockingElasticMachineProvisioning#startMachinesAsync(CapacityRequirements, long, TimeUnit)}
           //but still not in the list returned by {@link NonBlockingElasticMachineProvisioning#getDiscoveredMachinesAsync(long, TimeUnit)}
           
           final NonBlockingElasticMachineProvisioning oldMachineProvisioning = futureAgent.getMachineProvisioning();
           if (
                // checking for a bug in the implementation of machineProvisioning
               oldMachineProvisioning != null &&
               !MachinesSlaUtils.isAgentConformsToMachineProvisioningConfig(newAgent, oldMachineProvisioning.getConfig()) &&
               
               // ignore error if machine provisioning was modified
               oldMachineProvisioning == machineProvisioning) {
               
               throw new IllegalStateException(
                       MachinesSlaUtils.machineToString(newAgent.getMachine()) + " has been started but with the wrong zone or management settings"); 
           }
           
           // providing a grace period for provisionedAgents to update.
           throw new InconsistentMachineProvisioningException(new String[]{getProcessingUnit().getName()}, newAgent);
        }
    }

    /**
     * Eagerly allocates all unallocated capacity that match the specified SLA
     * @throws FailedToDiscoverMachinesException 
     * @throws WaitingForDiscoveredMachinesException 
     */
    private void allocateEagerCapacity(AbstractMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException {
        
        CapacityRequirementsPerAgent unallocatedCapacity = getUnallocatedCapacity(sla);
        
        // limit the memory to the maximum possible by this PU
        long maxAllocatedMemoryForPu = sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB();
        long allocatedMemoryForPu = MachinesSlaUtils.getMemoryInMB(getAllocatedCapacity(sla).getTotalAllocatedCapacity());

        // validate not breached max eager capacity
        if (allocatedMemoryForPu > maxAllocatedMemoryForPu) {
            throw new IllegalStateException(
                    "maxAllocatedMemoryForPu="+sla.getMaximumNumberOfMachines()+"*"+sla.getContainerMemoryCapacityInMB()+"="+
                    maxAllocatedMemoryForPu+" "+
                    "cannot be smaller than allocatedMemoryForPu="+getAllocatedCapacity(sla).toDetailedString());
        }
        long unallocatedMemory = MachinesSlaUtils.getMemoryInMB(unallocatedCapacity.getTotalAllocatedCapacity());
        long memoryToAllocate = Math.min(maxAllocatedMemoryForPu - allocatedMemoryForPu, unallocatedMemory);
        
        CapacityRequirements capacityToAllocate = 
            unallocatedCapacity.getTotalAllocatedCapacity()
            .set(new MemoryCapacityRequirement(memoryToAllocate));
        
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "capacityToAllocate=" + capacityToAllocate + " "+
                    "maxAllocatedMemoryForPu="+maxAllocatedMemoryForPu + " "+
                    "unallocatedCapacity="+unallocatedCapacity.toDetailedString() );
        }
        // allocate memory and CPU eagerly
        if (!capacityToAllocate.equalsZero()) { 
            allocateManualCapacity(sla, capacityToAllocate, unallocatedCapacity);
        }
        
        //validate not breached max eager capacity
        long allocatedMemoryForPuAfter = MachinesSlaUtils.getMemoryInMB(getAllocatedCapacity(sla).getTotalAllocatedCapacity());
        if ( allocatedMemoryForPuAfter > maxAllocatedMemoryForPu) {
            throw new IllegalStateException("allocatedMemoryForPuAfter="+allocatedMemoryForPuAfter+" greater than "+
                    "maxAllocatedMemoryForPu="+sla.getMaximumNumberOfMachines()+"*"+sla.getContainerMemoryCapacityInMB()+"="+maxAllocatedMemoryForPu+
                    "allocatedMemoryForPu="+allocatedMemoryForPu+"="+getAllocatedCapacity(sla).toDetailedString()+ " "+
                    "capacityToAllocate=" + capacityToAllocate + " "+
                    "maxAllocatedMemoryForPu="+maxAllocatedMemoryForPu + " "+
                    "unallocatedCapacity="+unallocatedCapacity.toDetailedString() );
        }
    }
    
    /**
     * Allocates the specified capacity on unallocated capacity that match the specified SLA
     * @throws MachinesSlaEnforcementInProgressException 
     */
    private void allocateManualCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirements capacityToAllocate) throws MachinesSlaEnforcementInProgressException {
        CapacityRequirementsPerAgent unallocatedCapacity = getUnallocatedCapacity(sla);
        allocateManualCapacity(sla, capacityToAllocate, unallocatedCapacity);
    }
    
    /**
     * Finds an agent that matches the SLA, and can satisfy the specified capacity to allocate.
     * 
     * The algorithm goes over all such machines and chooses the machine that after the allocation would have
     * the least amount of memory. So continuous unallocated blocks is left for the next allocation, which could be bigger.
     * 
     * @param capacityToAllocate - the missing capacity that requires allocation on shared machines.
     * @return the remaining capacity left for allocation(the rest has been allocated already)
     */
    private void allocateManualCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirements capacityToAllocate, CapacityRequirementsPerAgent unallocatedCapacity) {
        
        final BinPackingSolver solver = createBinPackingSolver(sla , unallocatedCapacity);
        
        solver.solveManualCapacityScaleOut(capacityToAllocate);

        allocateCapacity(sla, solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(sla, solver.getDeallocatedCapacityResult());                    
    }

    private void deallocateManualCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirements capacityToDeallocate) throws MachinesSlaEnforcementInProgressException {
        CapacityRequirementsPerAgent unallocatedCapacity = getUnallocatedCapacity(sla);
        final BinPackingSolver solver = createBinPackingSolver(sla , unallocatedCapacity);
        
        solver.solveManualCapacityScaleIn(capacityToDeallocate);

        allocateCapacity(sla, solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(sla, solver.getDeallocatedCapacityResult());                    
    }

    private void allocateNumberOfMachines(AbstractMachinesSlaPolicy sla, int numberOfFreeAgents) throws MachinesSlaEnforcementInProgressException {
        CapacityRequirementsPerAgent unallocatedCapacity = getUnallocatedCapacity(sla);
        allocateNumberOfMachines(sla, numberOfFreeAgents, unallocatedCapacity);
    }

    /**
     * Finds an agent that matches the SLA, and can satisfy the specified capacity to allocate.
     * 
     * The algorithm goes over all such machines and chooses the machine that after the allocation would have
     * the least amount of memory. So continuous unallocated blocks is left for the next allocation, which could be bigger.
     * 
     * @param numberOfMachines - the missing number of machines that are required.
     * @return the remaining number of machines (the rest has been allocated already)
     */
    private void allocateNumberOfMachines(AbstractMachinesSlaPolicy sla, int numberOfMachines, CapacityRequirementsPerAgent unallocatedCapacity) {

        final BinPackingSolver solver = createBinPackingSolver(sla, unallocatedCapacity);
        
        solver.solveNumberOfMachines(numberOfMachines);
        allocateCapacity(sla, solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(sla, solver.getDeallocatedCapacityResult());
    }

    private BinPackingSolver createBinPackingSolver(AbstractMachinesSlaPolicy sla, CapacityRequirementsPerAgent unallocatedCapacity) {
        final BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(sla.getContainerMemoryCapacityInMB());
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setAllocatedCapacityForPu(getAllocatedCapacity(sla));
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(sla.getMaximumNumberOfContainersPerMachine()*sla.getContainerMemoryCapacityInMB());
        solver.setMinimumNumberOfMachines(sla.getMinimumNumberOfMachines());
        
        // the higher the priority the less likely the machine to be scaled in.
        Map<String,Integer> scaleInPriorityPerAgentUid = new HashMap<String,Integer>();
        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();
        for (String agentUid : getAllocatedCapacity(sla).getAgentUids()) {
            int agentOrderToDeallocateContainers = 0;
            GridServiceAgent agent = agents.getAgentByUID(agentUid);
            if (agent != null) {
                if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                    // machine has management on it. It will not shutdown anyway, so try to scale in other
                    // machines first.
                    agentOrderToDeallocateContainers = 3;
                }
                else if (state.isAgentSharedWithOtherProcessingUnits(pu, agentUid)) {
                    // machine has other PUs on it. 
                    // try to scale in other machines first.
                    agentOrderToDeallocateContainers = 2;
                }
                else if (!MachinesSlaUtils.isAgentAutoShutdownEnabled(agent)) {
                    // machine cannot be shutdown by ESM
                    // try scaling in machines that we can shutdown first.
                    agentOrderToDeallocateContainers = 1;
                }
            }
            scaleInPriorityPerAgentUid.put(agentUid, agentOrderToDeallocateContainers);
        }
        solver.setAgentAllocationPriority(scaleInPriorityPerAgentUid);
        
        return solver;
    }
        
    /**
     * Calculates the total unused capacity (memory / CPU) on machines (that some of its capacity is already allocated by some PU). 
     * Returns only machines that match the specified SLA.
     * @throws MachinesSlaEnforcementInProgressException 
     */
    private CapacityRequirementsPerAgent getUnallocatedCapacity(AbstractMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException {
        
        CapacityRequirementsPerAgent physicalCapacity = getPhysicalProvisionedCapacity(sla);
        CapacityRequirementsPerAgent usedCapacity = state.getAllUsedCapacity();
        Map<String,List<String>> restrictedAgentUids = getRestrictedAgentUidsForPuWithReason(sla); 
        CapacityRequirementsPerAgent unallocatedCapacity = new CapacityRequirementsPerAgent();
        
        for (String agentUid : physicalCapacity.getAgentUids()) {
            
            if (!restrictedAgentUids.containsKey(agentUid)) { 
                
                // machine matches isolation and zone SLA 
                CapacityRequirements unallocatedCapacityOnAgent = 
                    physicalCapacity.getAgentCapacity(agentUid)
                    .subtract(usedCapacity.getAgentCapacityOrZero(agentUid));
                    
                unallocatedCapacity = 
                    unallocatedCapacity.add(agentUid, unallocatedCapacityOnAgent);
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("physicalCapacity="+physicalCapacity.toDetailedString()+" usedCapacity="+usedCapacity.toDetailedString()+" restrictedAgentUids="+restrictedAgentUids+" unallocatedCapacity="+unallocatedCapacity.toDetailedString());
        }
        
        return unallocatedCapacity;
    }

    /**
     * Calculates the total maximum capacity (memory/CPU) on all sla.getDiscoveredMachinesCache().getDiscoveredAgents()
     * @throws MachinesSlaEnforcementInProgressException 
     */
    private CapacityRequirementsPerAgent getPhysicalProvisionedCapacity(AbstractMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException {
        CapacityRequirementsPerAgent totalCapacity = new CapacityRequirementsPerAgent(); 
        for (final GridServiceAgent agent: sla.getDiscoveredMachinesCache().getDiscoveredAgents()) {
            if (agent.isDiscovered()) {
                
                totalCapacity = totalCapacity.add(
                        agent.getUid(), 
                        MachinesSlaUtils.getMachineTotalCapacity(agent, sla));
            }
        }
        return totalCapacity;
    }
    
    private static int numberOfMachines(CapacityRequirements capacityRequirements) {
        
        return capacityRequirements.getRequirement(new NumberOfMachinesCapacityRequirement().getType()).getNumberOfMachines();
    }

    private CapacityRequirementsPerAgent getCapacityMarkedForDeallocation(AbstractMachinesSlaPolicy sla) {
        return state.getCapacityMarkedForDeallocation(getKey(sla));
    }
    
    private Map<String,List<String>> getRestrictedAgentUidsForPuWithReason(AbstractMachinesSlaPolicy sla) {
        return state.getRestrictedAgentUids(getKey(sla));
    }

    private void setMachineIsolation(AbstractMachinesSlaPolicy sla) {
        state.setMachineIsolation(getKey(sla), sla.getMachineIsolation());
    }
    
    private int getNumberOfFutureAgents(AbstractMachinesSlaPolicy sla) {
        return state.getNumberOfFutureAgents(getKey(sla));
    }

    private void unmarkCapacityForDeallocation(CapacityMachinesSlaPolicy sla, String agentUid, CapacityRequirements agentCapacity) {
        state.unmarkCapacityForDeallocation(getKey(sla), agentUid, agentCapacity);
    }

    private void addFutureAgents(CapacityMachinesSlaPolicy sla, 
            FutureGridServiceAgent[] futureAgents, CapacityRequirements shortageCapacity) {
        state.addFutureAgents(getKey(sla), futureAgents, shortageCapacity);
    }

    private Collection<String> getAgentUidsGoingDown(AbstractMachinesSlaPolicy sla) {
        return state.getAgentUidsGoingDown(getKey(sla));
    }

    private Collection<GridServiceAgentFutures> getFutureAgents(AbstractMachinesSlaPolicy sla) {
        return state.getFutureAgents(getKey(sla));
    }

    private void unmarkCapacityForDeallocation(AbstractMachinesSlaPolicy sla, String agentUid, CapacityRequirements agentCapacity) {
        state.unmarkCapacityForDeallocation(getKey(sla), agentUid, agentCapacity);
    }

    private void deallocateAgentCapacity(AbstractMachinesSlaPolicy sla, String agentUid) {
        state.deallocateAgentCapacity(getKey(sla), agentUid);
    }
    
    private void agentGoingDown(AbstractMachinesSlaPolicy sla, GridServiceAgent agent) {
        state.agentGoingDown(getKey(sla),agent.getUid(),STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
    
    private void markAgentCapacityForDeallocation(AbstractMachinesSlaPolicy sla, String agentUid) {
        state.markAgentCapacityForDeallocation(getKey(sla), agentUid);
    }
    
    private Collection<GridServiceAgentFutures> getAllDoneFutureAgents(AbstractMachinesSlaPolicy sla) {
        return state.getAllDoneFutureAgents(getKey(sla));
    }
    
    private void removeFutureAgents(AbstractMachinesSlaPolicy sla, GridServiceAgentFutures doneFutureAgents) {
        state.removeFutureAgents(getKey(sla), doneFutureAgents);
    }

    private void allocateCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirementsPerAgent capacityToAllocate) {
        GridServiceAgents gridServiceAgents = pu.getAdmin().getGridServiceAgents();
        for (String agentUid : capacityToAllocate.getAgentUids()) {
            if (gridServiceAgents.getAgentByUID(agentUid) == null) {
                throw new IllegalArgumentException("Cannot allocate capacity on a removed agent " + agentUid);
            }
            state.allocateCapacity(getKey(sla), agentUid, capacityToAllocate.getAgentCapacity(agentUid));
            if (logger.isInfoEnabled()) {
                logger.info("allocating capacity "+capacityToAllocate.getAgentCapacity(agentUid) + " "+
                            "on " + MachinesSlaUtils.agentToString(pu.getAdmin(), agentUid) + " "+
                            "for " + pu.getName() + " "+sla.getGridServiceAgentZones());
            }
        }
    }

    private void markCapacityForDeallocation(AbstractMachinesSlaPolicy sla, CapacityRequirementsPerAgent capacityToMarkForDeallocation) {
        for (String agentUid : capacityToMarkForDeallocation.getAgentUids()) {
            state.markCapacityForDeallocation(getKey(sla), agentUid, capacityToMarkForDeallocation.getAgentCapacity(agentUid));
            if (logger.isInfoEnabled()) {
                logger.info("marking capacity for deallocation "+capacityToMarkForDeallocation.getAgentCapacity(agentUid) + " on " + MachinesSlaUtils.agentToString(pu.getAdmin(), agentUid));
            }
        }
    }

    private void completedStateRecovery(AbstractMachinesSlaPolicy sla) {
        state.completedStateRecovery(getKey(sla));
    }

    private boolean isCompletedStateRecovery(AbstractMachinesSlaPolicy sla) {
        return state.isCompletedStateRecovery(getKey(sla));
    }

    @Override
    public void recoveredStateOnEsmStart(ProcessingUnit otherPu) {
        state.recoveredStateOnEsmStart(otherPu);
    }

    @Override
    public boolean isRecoveredStateOnEsmStart(ProcessingUnit otherPu) {
        return state.isRecoveredStateOnEsmStart(otherPu);
    }

    @Override
    public Set<ZonesConfig> getGridServiceAgentsZones() {
        return state.getGridServiceAgentsZones(pu);
    }

    @Override
    public boolean replaceAllocatedCapacity(AbstractMachinesSlaPolicy sla) {
        return state.replaceAllocatedCapacity(getKey(sla), pu.getAdmin());
    }
}


