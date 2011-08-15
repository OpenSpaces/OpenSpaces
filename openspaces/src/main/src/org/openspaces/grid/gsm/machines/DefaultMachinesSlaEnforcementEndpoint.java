package org.openspaces.grid.gsm.machines;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

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
class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint, EagerMachinesSlaEnforcementEndpoint {

    private static final int START_AGENT_TIMEOUT_SECONDS = 10*60;
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

    private void validateEndpointNotDestroyed(ProcessingUnit pu) {
        if (state.isProcessingUnitDestroyed(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }
    
    public ClusterCapacityRequirements getAllocatedCapacity() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
       validateEndpointNotDestroyed(pu);
       return state.getAllocatedCapacity(pu);
    }

     
    public boolean isGridServiceAgentsPendingDeallocation() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        
        return !state.getCapacityMarkedForDeallocation(pu).equalsZero();
    }

    public boolean enforceSla(CapacityMachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        
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
        
        state.setMachineIsolation(pu, sla.getMachineIsolation());
        
        try {
            validateProvisionedMachines(sla);
            enforceSlaInternal(sla);
            return true;
        } catch (OperationInProgressException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (NeedMoreMachinesException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (NeedMoreCapacityException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (ScaleInObstructedException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (InconsistentMachineProvisioningException e) {
            logger.info(e, e);
            return false; // try again next time
        }
    }

    /**
     * Validates that the cloud has not "forgot about" machines without killing the agent on them.
     * This can happen when the connection with the cloud is lost and we cannot determine which machines are allocated for this pu by the cloud.
     * 
     * We added this method since in such condition the code behaves unexpectedly (all machines are unallocated automatically, and then some other pu tries to allocate them once the cloud is back online).
     * @param sla
     * @throws InconsistentMachineProvisioningException 
     */
    private void validateProvisionedMachines(AbstractMachinesSlaPolicy sla) throws InconsistentMachineProvisioningException {

        Collection<GridServiceAgent> discoveredAgents =sla.getProvisionedAgents();
        Collection<GridServiceAgent> undiscoveredAgents = new HashSet<GridServiceAgent>();
        for(GridServiceAgent agent : MachinesSlaUtils.convertAgentUidsToAgentsIfDiscovered(state.getAllocatedCapacity(pu).getAgentUids(),pu.getAdmin())) {
           
            if (!discoveredAgents.contains(agent)) {
                undiscoveredAgents.add(agent);
           }
        }
        if (undiscoveredAgents.size() > 0) {
            throw new InconsistentMachineProvisioningException("Machines " + MachinesSlaUtils.machinesToString(undiscoveredAgents)+ " are no longer provisioned by the cloud for pu "+ pu.getName());
        }
    }

    private void validateSla(AbstractMachinesSlaPolicy sla) {
        
        if (sla == null) {
            throw new IllegalArgumentException("SLA cannot be null");
        }
        
        if (sla.getContainerMemoryCapacityInMB() <= 0) {
            throw new IllegalArgumentException("Container memory capacity must be defined.");
        }
        
        if (sla.getMachineProvisioning() == null) {
            throw new IllegalArgumentException("machine provisioning cannot be null");
        }
        
        if (sla.getMachineIsolation() == null) {
            throw new IllegalArgumentException("machine isolation cannot be null");
        }
        
        if (sla.getMaximumNumberOfMachines() < 0) {
            throw new IllegalArgumentException("maximum number of machines cannot be " + sla.getMaximumNumberOfMachines());
        }
        
        if (sla.getMinimumNumberOfMachines() < 0) {
            throw new IllegalArgumentException("minimum number of machines cannot be " + sla.getMinimumNumberOfMachines());
        }
        
        if (sla.getMinimumNumberOfMachines() > sla.getMaximumNumberOfMachines()) {
            throw new IllegalArgumentException(
                    "minimum number of machines ("+sla.getMinimumNumberOfMachines()+
                    ") cannot be bigger than maximum number of machines ("+
                    sla.getMaximumNumberOfMachines()+")");
        }
        
        if (sla.getProvisionedAgents() == null) {
            throw new IllegalArgumentException("Provisioned agents cannot be null");
        }
    }
    
    public boolean enforceSla(EagerMachinesSlaPolicy sla)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        validateEndpointNotDestroyed(pu);
        validateSla(sla);
       
        state.setMachineIsolation(pu, sla.getMachineIsolation());
        
        try {
            validateProvisionedMachines(sla);
            enforceSlaInternal(sla);
            return true;
        } catch (OperationInProgressException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (NeedMoreMachinesException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (InconsistentMachineProvisioningException e) {
            logger.info(e, e);
            return false; // try again next time
        }
    }

    private void enforceSlaInternal(EagerMachinesSlaPolicy sla) throws OperationInProgressException, NeedMoreMachinesException, InconsistentMachineProvisioningException {
        
        updateFutureAgentsState(sla);
        updateFailedAndUnprovisionedMachinesState(sla);
        updateAgentsMarkedForDeallocationState(sla);
        
        unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(sla);
        
        //Eager scale out: allocate as many machines and as many CPU as possible
        allocateEagerCapacity(sla);
                
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        if (machineShortage > 0) {
            throw new NeedMoreMachinesException(machineShortage);
        }
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }
            
    private void enforceSlaInternal(CapacityMachinesSlaPolicy sla)
            throws OperationInProgressException, NeedMoreMachinesException, NeedMoreCapacityException, ScaleInObstructedException, InconsistentMachineProvisioningException {

        updateFutureAgentsState(sla);
        updateFailedAndUnprovisionedMachinesState(sla);
        updateAgentsMarkedForDeallocationState(sla);
        
        ClusterCapacityRequirements capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        ClusterCapacityRequirements capacityAllocated = state.getAllocatedCapacity(pu);
        
        if (state.getNumberOfFutureAgents(pu) > 0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending deallocation.");
        }
        
        CapacityRequirements target = sla.getCapacityRequirements();
            
        ClusterCapacityRequirements capacityAllocatedAndMarked = 
            capacityMarkedForDeallocation.add(capacityAllocated);
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        
        if (!capacityAllocatedAndMarked.getTotalAllocatedCapacity().equals(target) &&
            capacityAllocatedAndMarked.getTotalAllocatedCapacity().greaterOrEquals(target) &&
            machineShortage == 0) {
            
            logger.debug("Considering scale in: "+
                    "target is "+ target + " " +
                    "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                    "machines started " + state.getAllocatedCapacity(pu) + ", " + 
                    "machines pending deallocation " + state.getCapacityMarkedForDeallocation(pu));
            
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
                    state.unmarkCapacityForDeallocation(pu, agentUid, agentCapacity);
                    if (logger.isInfoEnabled()) {
                        GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                        if (agent != null) {
                        logger.info(
                                "machine agent " + agent.getMachine().getHostAddress() + " " +
                                "is no longer marked for deallocation in order to maintain capacity. "+
                                "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
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
            logger.info("Considering to start more machines inorder to reach target capacity:" + 
                    "target is "+ target +
                    "machines started " + state.getAllocatedCapacity(pu) + ", " + 
                    "machines pending deallocation " + state.getCapacityMarkedForDeallocation(pu));
            }
            
            CapacityRequirements shortageCapacity = getCapacityShortage(target);
            
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
                state.unmarkCapacityForDeallocation(pu, agentUid, requiredCapacity);
                shortageCapacity = shortageCapacity.subtract(requiredCapacity);
            }
            
           if (!shortageCapacity.equalsZero()) {
               allocateManualCapacity(shortageCapacity, sla);
               shortageCapacity = getCapacityShortage(target);               
           }
           
           if (!shortageCapacity.equalsZero()) {
               if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                   throw new NeedMoreCapacityException(shortageCapacity);
               }
               
                FutureGridServiceAgent[] futureAgents = sla.getMachineProvisioning().startMachinesAsync(
                    shortageCapacity,
                    START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                state.futureAgents(pu, futureAgents, shortageCapacity);
                
                logger.info(
                        "One or more new machine(s) is started in order to "+
                        "fill capacity shortage " + shortageCapacity + " " + 
                        "Allocated machine agents are: " + state.getAllocatedCapacity(pu) +" "+
                        "Pending future machine(s) requests " + state.getNumberOfFutureAgents(pu));
                
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
                allocateNumberOfMachines(machineShortage, sla);
                machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
            }
            
            if (machineShortage > 0) {
                
                if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                    throw new NeedMoreMachinesException(machineShortage);
                }
                
                
                // scale out to get to the minimum number of agents
                CapacityRequirements capacityRequirements = new CapacityRequirements(
                        new NumberOfMachinesCapacityRequirement(machineShortage));
                FutureGridServiceAgent[] futureAgents = sla.getMachineProvisioning().startMachinesAsync(
                        capacityRequirements,
                        START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                state.futureAgents(pu, futureAgents, capacityRequirements);
                
                logger.info(
                        machineShortage+ " new machine(s) is scheduled to be started in order to reach the minimum of " + 
                        sla.getMinimumNumberOfMachines() + " machines. " +
                        "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
                
            }
            
            // even if machineShortage is 0, we still need to come back to this method 
            // to check the capacity is satisfied (scale out)
            throw new OperationInProgressException();
        }
        else {
            logger.debug("No action required in order to enforce machines sla. "+
                    "target="+target + "| " + 
                    "started="+capacityAllocated.toDetailedString() + "| " +
                    "marked for deallocation="+capacityMarkedForDeallocation.toDetailedString() + "| " +
                    "#futures="+state.getNumberOfFutureAgents(pu) + " |" +
                    "#minimumMachines="+sla.getMinimumNumberOfMachines());        
        }
        

        if (!state.getCapacityMarkedForDeallocation(pu).equalsZero()) {
            // containers need to move to another machine
            throw new ScaleInObstructedException();
        }
        
        if (state.getNumberOfFutureAgents(pu) > 0) {
            // new machines need to be started
            throw new OperationInProgressException();
        }
        
        if (!state.getAgentUidsGoingDown(pu).isEmpty()) {
            // old machines need to complete shutdown
            throw new OperationInProgressException();
        }
    }

    private CapacityRequirements getCapacityShortage(CapacityRequirements target) throws OperationInProgressException {
        CapacityRequirements shortageCapacity = 
            target.subtractOrZero(state.getAllocatedCapacity(pu).getTotalAllocatedCapacity());
        
        // take into account expected machines into shortage calculate
        for (GridServiceAgentFutures futureAgents : state.getFutureAgents(pu)) {
                            
            CapacityRequirements expectedCapacityRequirements = futureAgents.getExpectedCapacity(); 
            for (CapacityRequirement shortageCapacityRequirement : shortageCapacity.getRequirements()) {
               
                CapacityRequirement expectedCapacityRequirement = expectedCapacityRequirements.getRequirement(shortageCapacityRequirement.getType());
                if (!shortageCapacityRequirement.equalsZero() && expectedCapacityRequirement.equalsZero()) {
                    // cannot determine expected capacity, it could be enough to satisfy shortage 
                    // and if that is the case, there is no point in declaring there is shortage.
                    throw new OperationInProgressException();
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
     */
    private int unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla) throws OperationInProgressException {
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        
        final ClusterCapacityRequirements capacityAllocated = state.getAllocatedCapacity(pu);
        ClusterCapacityRequirements capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);

        if (!capacityMarkedForDeallocation.equalsZero()) {
            
            // unmark machines that have only containers that are pending deallocation
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                if (machineShortage > 0 && !capacityAllocated.getAgentUids().contains(agentUid)) {
                
                    CapacityRequirements agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                    state.unmarkCapacityForDeallocation(pu, agentUid, agentCapacity);
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
     */
    private int getMachineShortageInOrderToReachMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla)
            throws OperationInProgressException {
        
        boolean cannotDetermineExpectedNumberOfMachines = false;
        final ClusterCapacityRequirements capacityAllocated = state.getAllocatedCapacity(pu);
        int machineShortage = sla.getMinimumNumberOfMachines() - capacityAllocated.getAgentUids().size();
        if (state.getNumberOfFutureAgents(pu) > 0) {
            // take into account expected machines into shortage calculate
            for (final GridServiceAgentFutures future : state.getFutureAgents(pu)) {
                
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
            throw new OperationInProgressException();
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
        final NonBlockingElasticMachineProvisioning machineProvisioning = sla.getMachineProvisioning();
        ClusterCapacityRequirements capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
            
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);

            if (agent == null) {
                state.deallocateAgentCapacity(pu, agentUid);
                logger.info("pu " + pu.getName() + " agent " + agentUid + " has shutdown.");
            }
            else if (MachinesSlaUtils.getMemoryInMB(state.getAllocatedCapacity(pu).getAgentCapacityOrZero(agentUid))>=sla.getContainerMemoryCapacityInMB()) {
                state.deallocateAgentCapacity(pu, agentUid);
                logger.info("pu " + pu.getName() +" is still allocated on agent " + agentUid);
            }
            else if (MachinesSlaUtils.getNumberOfChildContainersForProcessingUnit(agent,pu) == 0) {
                    
                if (!sla.isStopMachineSupported()) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since scale strategy " + sla.getScaleStrategyName()
                            + " does not support automatic start/stop of machines");
                    state.deallocateAgentCapacity(pu, agentUid);
                    continue;
                }
                
                if (!state.getAgentsStartedByMachineProvisioning().contains(agent)) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it was not started automatically");
                    state.deallocateAgentCapacity(pu, agentUid);
                    continue;
                }
                
                if (!machineProvisioning.isStartMachineSupported()) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since machine provisioning " + sla.getMachineProvisioning().getClass()
                            + "does not support automatic start/stop of machines");
                    state.deallocateAgentCapacity(pu, agentUid);
                    continue;
                }
                
                if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it is running management processes.");
                    state.deallocateAgentCapacity(pu, agentUid);
                    continue;
                }
                
                if (state.isAgentSharedWithOtherProcessingUnits(pu,agent.getUid())) {
                 // This is a bit like reference counting, the last pu to leave stops the machine
                    logger.info("Agent running on machine " + agent.getMachine().getHostAddress()
                            + " is not stopped since it is shared with other processing units.");
                    state.deallocateAgentCapacity(pu, agentUid);
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

               stopMachine(machineProvisioning, agent);
            }
        }
        
        cleanMachinesGoingDown(machineProvisioning);
    }

    private void stopMachine(final NonBlockingElasticMachineProvisioning machineProvisioning, GridServiceAgent agent) {
           // The machineProvisioning might not be the same one that started this agent,
           // Nevertheless, we expect it to be able to stop this agent.
           machineProvisioning.stopMachineAsync(
                   agent, 
                   STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
           
           state.agentGoingDown(pu,agent.getUid(),STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
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
        for(String agentUid: state.getAllocatedCapacity(pu).getAgentUids()) {
            if (agents.getAgentByUID(agentUid) == null) {
                logger.warn("Agent " + agentUid + " was killed unexpectedly.");
                state.markAgentCapacityForDeallocation(pu, agentUid);
            }
        }
        
        // mark for deallocation machines that are restricted for this PU
        Collection<String> restrictedMachines = state.getRestrictedAgentUidsForPu(pu);
        for(GridServiceAgent agent : MachinesSlaUtils.convertAgentUidsToAgents(state.getAllocatedCapacity(pu).getAgentUids(),pu.getAdmin())) {
            if (restrictedMachines.contains(agent.getUid())) {
                logger.info("Machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " is restricted for pu " + pu.getName());
                state.markAgentCapacityForDeallocation(pu, agent.getUid());
            }
        }
        
        for (GridServiceAgent agent : state.getAgentsStartedByMachineProvisioning()) {
            if (!agent.isDiscovered()) {
                state.removeIndicationThatAgentStartedByMachineProvisioning(agent);
            }
        }
    }
    
    private void cleanMachinesGoingDown(final NonBlockingElasticMachineProvisioning machineProvisioning) {

        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();

        // cleanup agents that were gracefully shutdown
        for (String agentUid: state.getAgentUidsGoingDown(pu)) {
            GridServiceAgent agent = agents.getAgentByUID(agentUid);
            if (agent == null) {
                logger.warn("Agent " + agentUid + " shutdown completed succesfully.");
                state.agentShutdownComplete(agentUid);
            }
            
            else if (state.isAgentUidGoingDownTimedOut(agentUid) && machineProvisioning.isStartMachineSupported()) {
                
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
     */
    private void updateFutureAgentsState(AbstractMachinesSlaPolicy sla) throws InconsistentMachineProvisioningException {
       
        // each futureAgents object contains an array of FutureGridServiceAgent
        // only when all future in the array is done, the futureAgents object is done.
        Collection<GridServiceAgentFutures> doneFutureAgentss = state.getAllDoneFutureAgents(pu);
        
        for (GridServiceAgentFutures doneFutureAgents : doneFutureAgentss) {
       
            Collection<GridServiceAgent> healthyAgents = null;
            try {
                // returns only futures that are healthy.
                healthyAgents = filterOnlyHealthyAgents(
                        sla.getProvisionedAgents(), 
                        sla.getMachineProvisioning(), 
                        doneFutureAgents);
            } catch (NewGridServiceAgentNotProvisionedYetException e) {
                // Better luck next time. We cannot accept an agent that has been started but not in sla.getProvisionedAgents()
                continue;
            }
            
            ClusterCapacityRequirements unallocatedCapacity = 
                getUnallocatedCapacityIncludeNewMachines(sla, healthyAgents);
                        
            //update state 1: allocate capacity
            if (numberOfMachines(doneFutureAgents.getExpectedCapacity()) > 0) {
                allocateNumberOfMachines(
                        numberOfMachines(doneFutureAgents.getExpectedCapacity()), 
                        sla, 
                        unallocatedCapacity);
            }
            else if (!doneFutureAgents.getExpectedCapacity().equalsZero()) {
                allocateManualCapacity(
                        sla, 
                        doneFutureAgents.getExpectedCapacity(), 
                        unallocatedCapacity); 
            }
            else {
                throw new InconsistentMachineProvisioningException("futureAgents expected capacity malformed. doneFutureAgents=" + 
                        doneFutureAgents.getExpectedCapacity());
            }
            
            //update state 2: remove futures from list
            state.removeFutureAgents(pu, doneFutureAgents);
            if (doneFutureAgents.getMachineProvisioning() != null &&
                doneFutureAgents.getMachineProvisioning().isStartMachineSupported()) {
                
                // this is not a mock future of existing machines. 
                // we really started this machine.
                for (GridServiceAgent newAgent : healthyAgents) {
                    state.addIndicationThatAgentStartedByMachineProvisioning(newAgent);
                }
            }
            
            // update state 3:
            // mark for shutdown new machines that are not marked by any processing unit
            // these are all future agents that should have been used by the binpacking solver. 
            // The fact that they have not been used suggests that the number of started 
            // machines by machineProvisioning is more than we asked for.
            for (GridServiceAgent newAgent : healthyAgents) {
                String agentUid = newAgent.getUid();
                CapacityRequirements agentCapacity = state.getAllocatedCapacity(pu).getAgentCapacityOrZero(agentUid);
                if (agentCapacity.equalsZero()) {
                    
                    if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                        logger.info("Agent running on machine " + newAgent.getMachine().getHostAddress()
                                + " is not stopped since machine provisioning " + sla.getMachineProvisioning().getClass()
                                + "does not support automatic start/stop of machines");
                        continue;
                    }
                    
                    logger.warn(
                            "Stopping machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " "+
                            "since it is not really needed by the PU that asked for it (processing unit " + pu.getName()+")." + 
                            "Agents provisioned by cloud: " + MachinesSlaUtils.machinesToString(sla.getProvisionedAgents()));
                    stopMachine(sla.getMachineProvisioning(), newAgent);
                }
            }
        
        }
        
    }

    private ClusterCapacityRequirements getUnallocatedCapacityIncludeNewMachines(
            AbstractMachinesSlaPolicy sla,
            Collection<GridServiceAgent> newMachines ) {
        // unallocated capacity = unallocated capacity + new machines
        ClusterCapacityRequirements unallocatedCapacity = getUnallocatedCapacity(sla);
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
     * @return true - if removed future agent from the state
     * @throws InconsistentMachineProvisioningException 
     */
    private Collection<GridServiceAgent> filterOnlyHealthyAgents(
            Collection<GridServiceAgent> provisionedAgents, 
            NonBlockingElasticMachineProvisioning machineProvisioning,
            GridServiceAgentFutures futureAgents) throws NewGridServiceAgentNotProvisionedYetException, InconsistentMachineProvisioningException {
        
        final Collection<GridServiceAgent> healthyAgents = new HashSet<GridServiceAgent>();
        final Collection<String> usedAgentUids = state.getAllUsedAgentUids();
        final Collection<String> usedAgentUidsForPu = state.getAllUsedAgentUidsForPu(pu);
                
        for (FutureGridServiceAgent futureAgent : futureAgents.getFutureGridServiceAgents()) {
            
            Throwable exception = null;
            GridServiceAgent newAgent = null;
            try {
                newAgent = futureAgent.get(); // throws an exception if something went wrong.
            } catch (ExecutionException e) {
                exception = e.getCause();
            } catch (TimeoutException e) {
                exception = e;
            }
            
            if (exception != null) {
                final String errorMessage = "Failed to start agent on new machine";
                if (logger.isWarnEnabled()) {
                    logger.warn(errorMessage , exception);
                }
            }
            else if (newAgent == null) {
                throw new InconsistentMachineProvisioningException("Future is done without exception, but returned a null agent");
            }
            else if (!newAgent.isDiscovered()) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " but it seems to shutdown unexpectedly."+
                            "The machine is ignored");
                }
            }
            else if (usedAgentUidsForPu.contains(newAgent.getUid())) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " which is already in use by this PU."+
                            "The machine is ignored");
                }
           }
           else if (usedAgentUids.contains(newAgent.getUid())) {
                    
                if (logger.isWarnEnabled()) {
                        logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " which is already in use by another PU."+
                            "This machine is ignored");
                }
           }
           else if (!provisionedAgents.contains(newAgent)) {
   
               //Handle the case of a new machine that was started with @{link {@link NonBlockingElasticMachineProvisioning#startMachinesAsync(CapacityRequirements, long, TimeUnit)}
               //but still not in the list returned by {@link NonBlockingElasticMachineProvisioning#getDiscoveredMachinesAsync(long, TimeUnit)}
               
               final NonBlockingElasticMachineProvisioning oldMachineProvisioning = futureAgent.getMachineProvisioning();
               if (
                    // checking for a bug in the implementation of machineProvisioning
                   oldMachineProvisioning != null &&
                   !MachinesSlaUtils.isAgentConformsToMachineProvisioningConfig(newAgent, oldMachineProvisioning.getConfig()) &&
                   
                   // ignore error if machine provisioning was modified
                   oldMachineProvisioning == machineProvisioning) {
                   
                   throw new InconsistentMachineProvisioningException(
                           MachinesSlaUtils.machineToString(newAgent.getMachine()) + " has been started but with the wrong zone or management settings"); 
               }
               
               // providing a grace period for provisionedAgents to update.
               if (!futureAgent.isTimedOut()) {
                   if (logger.isDebugEnabled()) {
                       logger.info(
                           "New machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                           " started, but still not confirmed to be provisioned.");
                   }
                   throw new NewGridServiceAgentNotProvisionedYetException(newAgent);
               }
               
               // check for machine provisioning changes
               if (!machineProvisioning.isStartMachineSupported()) {
                   // someone changed the configuration, and we got a machine that was started by the previous configuration.
                   logger.error("Cannot stop machine  " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " shut down the machine or the agent on the machine manually.");
               }
               
               else if (oldMachineProvisioning != machineProvisioning) {
                   // someone changed the configuration, and we got a machine that was started by the previous configuration.
                   logger.info("Stopping machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " since it was started by the previous configuration.");
                   stopMachine(machineProvisioning, newAgent);
               }
           }
           else {
               healthyAgents.add(newAgent);
           }

        }
        
        if (logger.isInfoEnabled()) {
            logger.info("Agents that were started by" + 
                    "machineProvisioning class=" + machineProvisioning.getClass() + " " +
                    "new agents: " + MachinesSlaUtils.machinesToString(healthyAgents) + " " +
                    "existing provisioned agents:" + MachinesSlaUtils.machinesToString(provisionedAgents));
        }
        
        return healthyAgents;
    }

    /**
     * Eagerly allocates all unallocated capacity that match the specified SLA
     */
    private void allocateEagerCapacity(AbstractMachinesSlaPolicy sla) {
        
        ClusterCapacityRequirements unallocatedCapacity = getUnallocatedCapacity(sla);
        
        // limit the memory to the maximum possible by this PU
        long maxAllocatedMemoryForPu = sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB();
        long allocatedMemoryForPu = MachinesSlaUtils.getMemoryInMB(state.getAllocatedCapacity(pu).getTotalAllocatedCapacity());

        // validate not breached max eager capacity
        if (allocatedMemoryForPu > maxAllocatedMemoryForPu) {
            throw new IllegalStateException(
                    "maxAllocatedMemoryForPu="+sla.getMaximumNumberOfMachines()+"*"+sla.getContainerMemoryCapacityInMB()+"="+
                    maxAllocatedMemoryForPu+" "+
                    "cannot be smaller than allocatedMemoryForPu="+state.getAllocatedCapacity(pu).toDetailedString());
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
        long allocatedMemoryForPuAfter = MachinesSlaUtils.getMemoryInMB(state.getAllocatedCapacity(pu).getTotalAllocatedCapacity());
        if ( allocatedMemoryForPuAfter > maxAllocatedMemoryForPu) {
            throw new IllegalStateException("allocatedMemoryForPuAfter="+allocatedMemoryForPuAfter+" greater than "+
                    "maxAllocatedMemoryForPu="+sla.getMaximumNumberOfMachines()+"*"+sla.getContainerMemoryCapacityInMB()+"="+maxAllocatedMemoryForPu+
                    "allocatedMemoryForPu="+allocatedMemoryForPu+"="+state.getAllocatedCapacity(pu).toDetailedString()+ " "+
                    "capacityToAllocate=" + capacityToAllocate + " "+
                    "maxAllocatedMemoryForPu="+maxAllocatedMemoryForPu + " "+
                    "unallocatedCapacity="+unallocatedCapacity.toDetailedString() );
        }
    }
    
    /**
     * Allocates the specified capacity on unallocated capacity that match the specified SLA
     */
    private void allocateManualCapacity(CapacityRequirements capacityToAllocate, AbstractMachinesSlaPolicy sla) {
        ClusterCapacityRequirements unallocatedCapacity = getUnallocatedCapacity(sla);
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
    private void allocateManualCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirements capacityToAllocate, ClusterCapacityRequirements unallocatedCapacity) {
        
        final BinPackingSolver solver = createBinPackingSolver(sla , unallocatedCapacity);
        
        solver.solveManualCapacityScaleOut(capacityToAllocate);

        allocateCapacity(solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(solver.getDeallocatedCapacityResult());                    
    }

    private void deallocateManualCapacity(AbstractMachinesSlaPolicy sla, CapacityRequirements capacityToDeallocate) {
        ClusterCapacityRequirements unallocatedCapacity = getUnallocatedCapacity(sla);
        final BinPackingSolver solver = createBinPackingSolver(sla , unallocatedCapacity);
        
        solver.solveManualCapacityScaleIn(capacityToDeallocate);

        allocateCapacity(solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(solver.getDeallocatedCapacityResult());                    
    }

    private void allocateCapacity(ClusterCapacityRequirements capacityToAllocate) {
        for (String agentUid : capacityToAllocate.getAgentUids()) {
            state.allocateCapacity(pu, agentUid, capacityToAllocate.getAgentCapacity(agentUid));
            if (logger.isDebugEnabled()) {
                logger.debug("allocating capacity "+capacityToAllocate.getAgentCapacity(agentUid) + " on agent uid " + agentUid);
            }
        }
    }
    
    private void markCapacityForDeallocation(ClusterCapacityRequirements capacityToMarkForDeallocation) {
        for (String agentUid : capacityToMarkForDeallocation.getAgentUids()) {
            state.markCapacityForDeallocation(pu, agentUid, capacityToMarkForDeallocation.getAgentCapacity(agentUid));
            if (logger.isDebugEnabled()) {
                logger.debug("marking capacity for deallocation "+capacityToMarkForDeallocation.getAgentCapacity(agentUid) + " on agent uid " + agentUid);
            }
        }
    }

    private void allocateNumberOfMachines(int numberOfFreeAgents, AbstractMachinesSlaPolicy sla) {
        ClusterCapacityRequirements unallocatedCapacity = getUnallocatedCapacity(sla);
        allocateNumberOfMachines(numberOfFreeAgents, sla, unallocatedCapacity);
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
    private void allocateNumberOfMachines(int numberOfMachines, AbstractMachinesSlaPolicy sla, ClusterCapacityRequirements unallocatedCapacity) {

        final BinPackingSolver solver = createBinPackingSolver(sla, unallocatedCapacity);
        
        solver.solveNumberOfMachines(numberOfMachines);
        allocateCapacity(solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(solver.getDeallocatedCapacityResult());
    }

    private BinPackingSolver createBinPackingSolver(AbstractMachinesSlaPolicy sla, ClusterCapacityRequirements unallocatedCapacity) {
        final BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(sla.getContainerMemoryCapacityInMB());
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setAllocatedCapacityForPu(state.getAllocatedCapacity(pu));
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuPerMachineInMB(sla.getMaximumNumberOfContainersPerMachine()*sla.getContainerMemoryCapacityInMB());
        solver.setMinimumNumberOfMachines(sla.getMinimumNumberOfMachines());
        
        // the higher the priority the less likely the machine to be scaled in.
        Map<String,Integer> scaleInPriorityPerAgentUid = new HashMap<String,Integer>();
        GridServiceAgents agents = pu.getAdmin().getGridServiceAgents();
        for (String agentUid : state.getAllocatedCapacity(pu).getAgentUids()) {
            int agentPriority = 0;
            GridServiceAgent agent = agents.getAgentByUID(agentUid);
            if (agent != null) {
                if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                    // machine has management on it. It will not shutdown anyway, so try to scale in other
                    // machines first.
                    agentPriority = 3;
                }
                else if (state.isAgentSharedWithOtherProcessingUnits(pu, agentUid)) {
                    // machine has other PUs on it. 
                    // try to scale in other machines first.
                    agentPriority = 2;
                }
                else if (!state.getAgentsStartedByMachineProvisioning().contains(agent)) {
                    // machine was not started by ESM, so it cannot be shutdown by ESM
                    // try scaling in machines that we can shutdown first.
                    agentPriority = 1;
                }
            }
            scaleInPriorityPerAgentUid.put(agentUid, agentPriority);
        }
        solver.setAgentAllocationPriority(scaleInPriorityPerAgentUid);
        
        return solver;
    }
    
    /**
     * Calculates the total unused capacity (memory / CPU) on machines (that some of its capacity is already allocated by some PU). 
     * Returns only machines that match the specified SLA.
     */
    private ClusterCapacityRequirements getUnallocatedCapacity(AbstractMachinesSlaPolicy sla) {
        
        ClusterCapacityRequirements physicalCapacity = getPhysicalProvisionedCapacity(sla);
        ClusterCapacityRequirements usedCapacity = state.getAllUsedCapacity();
        Collection<String> restrictedAgentUids = state.getRestrictedAgentUidsForPu(pu); 
        ClusterCapacityRequirements unallocatedCapacity = new ClusterCapacityRequirements();
        
        for (String agentUid : physicalCapacity.getAgentUids()) {
            
            if (!restrictedAgentUids.contains(agentUid)) { 
                
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
     * Calculates the total maximum capacity (memory/CPU) on all sla.getProvisionedAgents()
     */
    private ClusterCapacityRequirements getPhysicalProvisionedCapacity(AbstractMachinesSlaPolicy sla) {
        ClusterCapacityRequirements totalCapacity = new ClusterCapacityRequirements(); 
        for (final GridServiceAgent agent: sla.getProvisionedAgents()) {
            if (agent.isDiscovered()) {
                
                totalCapacity = totalCapacity.add(
                        agent.getUid(), 
                        MachinesSlaUtils.getMachineTotalCapacity(agent, sla));
            }
        }
        return totalCapacity;
    }

    private abstract static class MachinesSlaEnforcementException  extends Exception {

        private static final long serialVersionUID = 1L;

        public MachinesSlaEnforcementException(String message) {
            super(message);
        }

        /**
         * Override the method to avoid expensive stack build and synchronization,
         * since no one uses it anyway.
         */
        @Override
        public Throwable fillInStackTrace()
        {
            return null;
        }    
    }
    
    private static class OperationInProgressException extends MachinesSlaEnforcementException  {
        
        private static final long serialVersionUID = 1L;

        OperationInProgressException() {
            super("Machines SLA enforcement is in progress");        
        }
        
    }

    private static class NeedMoreCapacityException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        NeedMoreCapacityException(CapacityRequirements capacityShortage) {
            super("Cannot enforce Machines SLA since there are not enough machines available. "+
                  "Need more machines with " + capacityShortage);        
        }
    }
    
    private static class NeedMoreMachinesException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        NeedMoreMachinesException(int machineShortage) {
            super("Cannot enforce Machines SLA since there are not enough machines available. "+
                  "Need " + machineShortage + " more machines");
        }
    }
    
    private static class ScaleInObstructedException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        ScaleInObstructedException() {
            super("Cannot enforce Machines SLA since there are agents that need to be scaled in, but they are still running containers");
        }
    }

    private static class NewGridServiceAgentNotProvisionedYetException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        NewGridServiceAgentNotProvisionedYetException(GridServiceAgent agent) {
            super("Grid Service Agent " + MachinesSlaUtils.machineToString(agent.getMachine())  + " has been started but not discovered yet by the machine provisioning.");
        }
    }
    
    private static int numberOfMachines(CapacityRequirements capacityRequirements) {
        
        return capacityRequirements.getRequirement(new NumberOfMachinesCapacityRequirement().getType()).getNumberOfMachines();
    }
    
    private static class InconsistentMachineProvisioningException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        InconsistentMachineProvisioningException(String message) {
            super(message);
        }
    }
}


