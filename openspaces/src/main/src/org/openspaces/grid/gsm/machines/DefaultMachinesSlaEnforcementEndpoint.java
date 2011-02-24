package org.openspaces.grid.gsm.machines;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
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
    
    public AggregatedAllocatedCapacity getAllocatedCapacity() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
       validateEndpointNotDestroyed(pu);
       return state.getAllocatedCapacity(pu);
    }

     
    public boolean isGridServiceAgentsPendingDeallocation() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        
        return !state.getCapacityMarkedForDeallocation(pu).equalsZero();
    }

    public boolean enforceSla(CapacityMachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        
        if (sla == null) {
            throw new IllegalArgumentException("SLA cannot be null");
        }
        
        if (sla.getCpu() < 0 ) {
            throw new IllegalArgumentException("CPU cannot be negative");
        }
        
        if (sla.getMemoryCapacityInMB() < 0) {
            throw new IllegalArgumentException("Memory capacity cannot be negative");
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
        state.setMachineIsolation(pu, sla.getMachineIsolation());
        
        
        try {
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
        }
    }
    
    public boolean enforceSla(EagerMachinesSlaPolicy sla)
            throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        if (sla == null) {
            throw new IllegalArgumentException("SLA cannot be null");
        }
        
        if (sla.getContainerMemoryCapacityInMB() <= 0) {
            throw new IllegalArgumentException("Container memory capacity must be defined.");
        }
        
        if (sla.getMinimumNumberOfMachines() > sla.getMaximumNumberOfMachines()) {
            throw new IllegalArgumentException("Minimum number of machines cannot be more than maximum number of machines.");
        }
        
        if (sla.getMachineProvisioning() == null) {
            throw new IllegalArgumentException("machine provisioning cannot be null");
        }
        
        if (sla.getMachineIsolation() == null) {
            throw new IllegalArgumentException("machine isolation cannot be null");
        }
        state.setMachineIsolation(pu, sla.getMachineIsolation());
        
        try {
            enforceSlaInternal(sla);
            return true;
        } catch (OperationInProgressException e) {
            logger.info(e, e);
            return false; // try again next time
        } catch (NeedMoreMachinesException e) {
            logger.info(e, e);
            return false; // try again next time
        }
    }

    private void enforceSlaInternal(EagerMachinesSlaPolicy sla) throws OperationInProgressException, NeedMoreMachinesException {
        
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
            throws OperationInProgressException, NeedMoreMachinesException, NeedMoreCapacityException, ScaleInObstructedException {

        updateFutureAgentsState(sla);
        updateFailedAndUnprovisionedMachinesState(sla);
        updateAgentsMarkedForDeallocationState(sla);
        
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        AggregatedAllocatedCapacity capacityAllocated = state.getAllocatedCapacity(pu);
        
        if (state.getNumberOfFutureAgents(pu) > 0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending deallocation.");
        }
        
        AllocatedCapacity target =
            new AllocatedCapacity(
                    MachinesSlaUtils.convertCpuCoresFromDoubleToFraction(sla.getCpu()),
                    sla.getMemoryCapacityInMB());
        
        AggregatedAllocatedCapacity capacityAllocatedAndMarked = 
            capacityMarkedForDeallocation.add(capacityAllocated);
        
        if (capacityAllocatedAndMarked.getTotalAllocatedCapacity().moreThanSatisfies(target) &&
            capacityAllocatedAndMarked.getAgentUids().size() > sla.getMinimumNumberOfMachines()) {
            
            logger.debug("Considering scale in: "+
                    "target is "+ target + " " +
                    "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                    "machines started " + state.getAllocatedCapacity(pu) + ", " + 
                    "machines pending deallocation " + state.getCapacityMarkedForDeallocation(pu));
            
            // scale in
            AllocatedCapacity surplusCapacity = 
                capacityAllocatedAndMarked.getTotalAllocatedCapacity().subtract(target);           
            int surplusMachines = capacityAllocatedAndMarked.getAgentUids().size() - sla.getMinimumNumberOfMachines();
            
            // adjust surplusMemory based on agents marked for deallocation
            // remove mark if it would cause surplus to be below zero
            // remove mark if it would reduce the number of machines below the sla minimum.
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {

                AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                if (surplusCapacity.satisfies(agentCapacity) &&
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

            for (GridServiceAgent agent : sortManagementLast(capacityAllocated.getAgentUids())) {
                AllocatedCapacity agentCapacity = capacityAllocated.getAgentCapacity(agent.getUid());
                
                if (surplusCapacity.satisfies(agentCapacity) &&
                    surplusMachines > 0) {

                    // scale in machine
                    state.markCapacityForDeallocation(pu, agent.getUid(), agentCapacity);
                    surplusCapacity = surplusCapacity.subtract(agentCapacity);
                    surplusMachines --;
                    logger.info(
                            "Machine agent " + agent.getMachine().getHostAddress() + " is marked for deallocation in order to reduce capacity. "+
                            "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
                }
            }
        }

        else if (capacityAllocated.getAgentUids().size() <  sla.getMinimumNumberOfMachines()) {
            
            logger.info("Considering to start more machines to reach required minimum number of machines: " + 
                    capacityAllocated + " started, " +
                    capacityMarkedForDeallocation + " marked for deallocation, " +
                    sla.getMinimumNumberOfMachines() + " is the required minimum number of machines."
            );
            
               
            unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(sla);
            
            int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
            
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
                
                throw new OperationInProgressException();
            }
        }
        
        else if (!capacityAllocatedAndMarked.getTotalAllocatedCapacity().satisfies(target)) {
            
            // scale out

            if (logger.isInfoEnabled()) {
            logger.info("Considering to start more machines inorder to reach target capacity:" + 
                    "target is "+ target +
                    "machines started " + state.getAllocatedCapacity(pu) + ", " + 
                    "machines pending deallocation " + state.getCapacityMarkedForDeallocation(pu));
            }
            
            // unmark all machines pending deallocation              
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                AllocatedCapacity agentCapacity =  capacityMarkedForDeallocation.getAgentCapacity(agentUid);
                if (logger.isInfoEnabled()) {
                    GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
                    if (agent != null) {
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for deallocation in order to maintain capacity.");
                    }
                }
                state.unmarkCapacityForDeallocation(pu, agentUid, agentCapacity);
            }
            
            AllocatedCapacity shortageCapacity = 
                target.subtractOrZero(capacityAllocatedAndMarked.getTotalAllocatedCapacity());
            
            // take into account expected machines into shortage calculate
            for (GridServiceAgentFutures futureAgents : state.getFutureAgents(pu)) {
                                
                AllocatedCapacity expectedCapacity = futureAgents.getExpectedCapacity(); 
                if (!shortageCapacity.isMemoryEqualsZero() && expectedCapacity.isMemoryEqualsZero()) {
                    // cannot determine expected memory, it could be enough to satisfy shortage
                    throw new OperationInProgressException();
                }

                if (!shortageCapacity.isCpuCoresEqualsZero() && expectedCapacity.isCpuCoresEqualsZero()) {
                 // cannot determine expected cpu cores, it could be enough to satisfy shortage
                    throw new OperationInProgressException();
                }
                
                shortageCapacity = 
                    shortageCapacity.subtractOrZero(expectedCapacity);
            }

           if (!shortageCapacity.equalsZero()) {
               shortageCapacity = allocateManualCapacity(shortageCapacity, sla);
           }
           
           if (!shortageCapacity.equalsZero()) {
               if (!sla.getMachineProvisioning().isStartMachineSupported()) {
                   throw new NeedMoreCapacityException(shortageCapacity);
               }
               
                CapacityRequirements capacityRequirements = shortageCapacity.toCapacityRequirements();
                FutureGridServiceAgent[] futureAgents = sla.getMachineProvisioning().startMachinesAsync(
                    capacityRequirements,
                    START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                state.futureAgents(pu, futureAgents, capacityRequirements);
                
                logger.info(
                        "One or more new machine(s) is started in order to "+
                        "fill capacity shortage " + shortageCapacity + 
                        "Allocated machine agents are: " + state.getAllocatedCapacity(pu) +" "+
                        "Pending future machine(s) requests " + state.getNumberOfFutureAgents(pu));
                
           }
            
        }
        else {
            logger.debug("No action required in order to enforce machines sla. "+
                    "target="+target + ", " + 
                    "started="+capacityAllocated + ", " +
                    "marked for deallocation="+capacityMarkedForDeallocation + ", " +
                    "#futures="+state.getNumberOfFutureAgents(pu) + " " +
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

    /**
     * if minimum number of machines is breached then
     * unmark machines that have only containers that are pending for deallocation
     */
    private void unmarkAgentsMarkedForDeallocationToSatisfyMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla) throws OperationInProgressException {
        
        int machineShortage = getMachineShortageInOrderToReachMinimumNumberOfMachines(sla);
        
        final AggregatedAllocatedCapacity capacityAllocated = state.getAllocatedCapacity(pu);
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);

        if (!capacityMarkedForDeallocation.equalsZero()) {
            
            // unmark machines that have only containers that are pending deallocation
            for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                if (machineShortage > 0 && !capacityAllocated.getAgentUids().contains(agentUid)) {
                
                    AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
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
    }

    /**
     * @return minimumNumberOfMachines - allocatedMachines - futureMachines
     */
    private int getMachineShortageInOrderToReachMinimumNumberOfMachines(AbstractMachinesSlaPolicy sla)
            throws OperationInProgressException {
        
        final AggregatedAllocatedCapacity capacityAllocated = state.getAllocatedCapacity(pu);
        int machineShortage = sla.getMinimumNumberOfMachines() - capacityAllocated.getAgentUids().size();
        if (state.getNumberOfFutureAgents(pu) > 0) {
            // take into account expected machines into shortage calculate
            for (final GridServiceAgentFutures future : state.getFutureAgents(pu)) {
                
                final int expectedNumberOfMachines = future.getExpectedNumberOfMachines();
                if (expectedNumberOfMachines == 0) {
                    throw new OperationInProgressException();
                }
                machineShortage -= expectedNumberOfMachines;
            }
        }
        return machineShortage;
    }

    private Collection<GridServiceAgent> sortManagementLast(Iterable<String> agentUids) {
        
        return MachinesSlaUtils.sortManagementLast(MachinesSlaUtils.convertAgentUidsToAgents(agentUids, pu.getAdmin()));
    }
    
    /**
     * Kill agents marked for deallocation that no longer manage containers. 
     * @param sla 
     * @param machineProvisioning
     */
    private void updateAgentsMarkedForDeallocationState(AbstractMachinesSlaPolicy sla) {
        final NonBlockingElasticMachineProvisioning machineProvisioning = sla.getMachineProvisioning();
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
            
            AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
            
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);

            if (agent == null) {
                state.deallocateAgentCapacity(pu, agentUid);
                logger.info("pu " + pu.getName() + " deallocated " + agentCapacity + " from agent " + agentUid + " since it has shutdown.");
            }
            else if (MachinesSlaUtils.getNumberOfChildContainersForProcessingUnit(agent,pu) == 0) {
                    
                state.deallocateCapacity(pu, agentUid, agentCapacity);

                logger.info("pu " + pu.getName() + " deallocated " + agentCapacity + " from machine " + agent.getMachine().getHostAddress());
            
            
                // If agent is not used, then shut it down.
                // This is a bit like reference counting, the last pu to leave stops the machine
                if (sla.isStopMachineSupported() &&
                    state.getAgentsStartedByMachineProvisioning().contains(agent) &&
                    machineProvisioning.isStartMachineSupported() &&
                    !MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine()) &&
                    !state.isAgentSharedWithOtherProcessingUnits(pu,agent.getUid())) {
                     
                    // double check that agent is not used
                    Set<Long> childProcessesIds = MachinesSlaUtils.getChildProcessesIds(agent);
                    if (!childProcessesIds.isEmpty()) {
                        // this is unexpected. We already checked for management processes and other PU processes.
                        logger.warn("Agent " + agent.getUid() + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " cannot be shutdown due to the following child processes: " + childProcessesIds);
                    }
                    else {
                        logger.info(
                               "Agent " + agent.getUid() + " on machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " is no longer in use by any processing unit. "+
                               "It is going down!");

                       stopMachine(machineProvisioning, agent);
                    }
                }
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
                //state.deallocateAgentCapacity(pu, agentUid);
            }
        }
        
        /*
        // deallocate all failed machines
        // we can deallocate machines only if they are empty (no containers)
        // we know they are empty since they are not discovered
        for(String agentUid: state.getCapacityMarkedForDeallocation(pu).getAgentUids()) {
            if (agents.getAgentByUID(agentUid) == null) {
                logger.warn("Agent " + agentUid + " was killed unexpectedly.");
                state.deallocateAgentCapacity(pu, agentUid);
            }
        }
        */
        
        // mark for deallocation machines that are no longer in the SLA provisioned agents
        Set<GridServiceAgent> discoveredAgents = new HashSet<GridServiceAgent>(Arrays.asList(sla.getProvisionedAgents()));
        for(GridServiceAgent agent : MachinesSlaUtils.convertAgentUidsToAgents(state.getAllocatedCapacity(pu).getAgentUids(),pu.getAdmin())) {
            if (!discoveredAgents.contains(agent)) {
                logger.info("Machine " + MachinesSlaUtils.machineToString(agent.getMachine())+ " is no longer provisioned for pu " + pu.getName());
                state.markAgentCapacityForDeallocation(pu, agent.getUid());
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
     */
    private void updateFutureAgentsState(AbstractMachinesSlaPolicy sla) {
       
        for (GridServiceAgentFutures futureAgents : state.getAllDoneFutureAgents(pu)) {
       
            //TODO: Split this into two method calls.
            boolean isAllMachinesAreProvisioned = removeFailedFutureAgents(
                    sla.getProvisionedAgents(), 
                    sla.getMachineProvisioning(), 
                    futureAgents);
            
            if (isAllMachinesAreProvisioned) {
                
                AggregatedAllocatedCapacity unallocatedCapacity = 
                    getUnallocatedCapacityIncludeNewMachines(
                        sla,
                        futureAgents.getGridServiceAgents());
                            
                //allocate capacity
                if (futureAgents.getExpectedNumberOfMachines() > 0) {
                    allocateNumberOfMachines(futureAgents.getExpectedNumberOfMachines(), sla, unallocatedCapacity);
                }
                else if (!futureAgents.getExpectedCapacity().equalsZero()) {
                    allocateManualCapacity(sla, futureAgents.getExpectedCapacity(), unallocatedCapacity); 
                }
                else {
                    throw new IllegalStateException("futureAgents expected capacity malformed");
                }
                
                state.removeFutureAgents(pu, futureAgents);
                
                // mark for shutdown new machines that are not marked by any processing unit
                // these are all future agents that should have been used by the solver. The fact that they have not been used
                // suggests that the number of started machines is too big.
                for (GridServiceAgent newAgent : futureAgents.getGridServiceAgents()) {
                    if (state.getAllocatedCapacity(pu).getAgentCapacityOrZero(newAgent.getUid()).equalsZero()) {
                        logger.warn(
                                "Stopping machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " "+
                                "since it is not really needed by the PU that asked for it (processing unit " + pu.getName()+")");
                        stopMachine(sla.getMachineProvisioning(), newAgent);
                    }
                }
            }
        }
        
    }

    private AggregatedAllocatedCapacity getUnallocatedCapacityIncludeNewMachines(
            AbstractMachinesSlaPolicy sla,
            Collection<GridServiceAgent> newMachines ) {
        // unallocated capacity = unallocated capacity + new machines
        AggregatedAllocatedCapacity unallocatedCapacity = getUnallocatedCapacity(sla);
        for (GridServiceAgent newAgent : newMachines) {
            if (logger.isInfoEnabled()) {
                logger.info("Agent started and provisioned succesfully on a new machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()));
            }
            unallocatedCapacity = 
                unallocatedCapacity.add(
                    newAgent.getUid(), 
                    MachinesSlaUtils.getMachineTotalCapacity(newAgent,sla));
        }
        return unallocatedCapacity;
    }

    /**
     * @return true - if removed future agent from the state
     */
    private boolean removeFailedFutureAgents(
            GridServiceAgent[] provisionedAgents2, 
            NonBlockingElasticMachineProvisioning machineProvisioning,
            GridServiceAgentFutures futureAgents) {
        
        final Collection<String> usedAgentUids = state.getAllUsedAgentUids();
        final Collection<String> usedAgentUidsForPu = state.getAllUsedAgentUidsForPu(pu);
        Set<GridServiceAgent> provisionedAgents = new HashSet<GridServiceAgent>(Arrays.asList(provisionedAgents2));
        
        boolean updateFutureAgentsState = true;
        
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
                futureAgents.removeFutureAgent(futureAgent);
            }
            else if (newAgent == null) {
                throw new IllegalStateException("Future is done without exception, but returned a null agent");
            }
            else if (!newAgent.isDiscovered()) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " but it seems to shutdown unexpectedly."+
                            "The machine is ignored");
                }
                futureAgents.removeFutureAgent(futureAgent);
            }
            else if (usedAgentUidsForPu.contains(newAgent.getUid())) {
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " which is already in use by this PU."+
                            "The machine is ignored");
                }
                futureAgents.removeFutureAgent(futureAgent);
           }
           else if (usedAgentUids.contains(newAgent.getUid())) {
                    
                if (logger.isWarnEnabled()) {
                        logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                            " which is already in use by another PU."+
                            "This machine is ignored");
                }
                futureAgents.removeFutureAgent(futureAgent);
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
                   
                   throw new IllegalStateException(
                           MachinesSlaUtils.machineToString(newAgent.getMachine()) + " has been started but with the wrong zone or management settings"); 
               }
               
               // providing a grace period for provisionedAgents to update.
               if (!futureAgent.isTimedOut()) {
                   updateFutureAgentsState = false;
                   if (logger.isDebugEnabled()) {
                       logger.info(
                           "New machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) +
                           " started, but still not confirmed to be provisioned.");
                   }
                   break;
               }
               
               // check for machine provisioning changes
               else if (!machineProvisioning.isStartMachineSupported()) {
                   futureAgents.removeFutureAgent(futureAgent);
                   // someone changed the configuration, and we got a machine that was started by the previous configuration.
                   logger.error("Cannot stop machine  " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " shut down the machine or the agent on the machine manually.");
               }
               
               else if (oldMachineProvisioning != machineProvisioning) {
                   futureAgents.removeFutureAgent(futureAgent);
                   // someone changed the configuration, and we got a machine that was started by the previous configuration.
                   logger.info("Stopping machine " + MachinesSlaUtils.machineToString(newAgent.getMachine()) + " since it was started by the previous configuration.");
                   stopMachine(machineProvisioning, newAgent);
               }

           }
        }
        return updateFutureAgentsState;
    }

    /**
     * Eagerly allocates all unallocated capacity that match the specified SLA
     */
    private void allocateEagerCapacity(AbstractMachinesSlaPolicy sla) {
        AggregatedAllocatedCapacity unallocatedCapacity = getUnallocatedCapacity(sla);
        allocateManualCapacity(sla,unallocatedCapacity.getTotalAllocatedCapacity(), unallocatedCapacity);
    }
    
    /**
     * Allocates the specified capacity on unallocated capacity that match the specified SLA
     */
    private AllocatedCapacity allocateManualCapacity(AllocatedCapacity capacityToAllocate, AbstractMachinesSlaPolicy sla) {
        AggregatedAllocatedCapacity unallocatedCapacity = getUnallocatedCapacity(sla);
        return allocateManualCapacity(sla, capacityToAllocate, unallocatedCapacity);
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
    private AllocatedCapacity allocateManualCapacity(AbstractMachinesSlaPolicy sla, AllocatedCapacity capacityToAllocate, AggregatedAllocatedCapacity unallocatedCapacity) {
        
        final BinPackingSolver solver = createBinPackingSolver(sla , unallocatedCapacity);
        
        solver.solveManualCapacity(capacityToAllocate);

        allocateCapacity(solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(solver.getDeallocatedCapacityResult());
        
        // When a container is relocated, its memory capacity is both deallocated and allocated, which compensates to zero
        // Remember that AllocatedCapacity cannot be negative, so first we add only then subtract.
        AllocatedCapacity remainingCapacityToAllocate = 
            capacityToAllocate
            .add(solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity())
            .subtract(solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        
        return remainingCapacityToAllocate;
                    
    }

    private void allocateCapacity(AggregatedAllocatedCapacity capacityToAllocate) {
        for (String agentUid : capacityToAllocate.getAgentUids()) {
            state.allocateCapacity(pu, agentUid, capacityToAllocate.getAgentCapacity(agentUid));
        }
    }
    
    private void markCapacityForDeallocation(AggregatedAllocatedCapacity capacityToMarkForDeallocation) {
        for (String agentUid : capacityToMarkForDeallocation.getAgentUids()) {
            state.markCapacityForDeallocation(pu, agentUid, capacityToMarkForDeallocation.getAgentCapacity(agentUid));
        }
    }

    private void allocateNumberOfMachines(int numberOfFreeAgents, AbstractMachinesSlaPolicy sla) {
        AggregatedAllocatedCapacity unallocatedCapacity = getUnallocatedCapacity(sla);
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
    private void allocateNumberOfMachines(int numberOfMachines, AbstractMachinesSlaPolicy sla, AggregatedAllocatedCapacity unallocatedCapacity) {

        final BinPackingSolver solver = createBinPackingSolver(sla, unallocatedCapacity);
        
        solver.solveNumberOfMachines(numberOfMachines);
        allocateCapacity(solver.getAllocatedCapacityResult());
        markCapacityForDeallocation(solver.getDeallocatedCapacityResult());
    }

    private BinPackingSolver createBinPackingSolver(AbstractMachinesSlaPolicy sla, AggregatedAllocatedCapacity unallocatedCapacity) {
        final BinPackingSolver solver = new BinPackingSolver();
        solver.setLogger(logger);
        solver.setContainerMemoryCapacityInMB(sla.getContainerMemoryCapacityInMB());
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setAllocatedCapacityForPu(state.getAllocatedCapacity(pu));
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(sla.getMaximumNumberOfMachines()*sla.getContainerMemoryCapacityInMB());
        sla.setMinimumNumberOfMachines(sla.getMinimumNumberOfMachines());
        return solver;
    }
    
    /**
     * Calculates the total unused capacity (memory / CPU) on machines (that some of its capacity is already allocated by some PU). 
     * Returns only machines that match the specified SLA.
     */
    private AggregatedAllocatedCapacity getUnallocatedCapacity(AbstractMachinesSlaPolicy sla) {
        
        AggregatedAllocatedCapacity physicalCapacity = getPhysicalProvisionedCapacity(sla);
        AggregatedAllocatedCapacity usedCapacity = state.getAllUsedCapacity();
        Collection<String> restrictedAgentUids = state.getRestrictedAgentUidsForPu(pu); 
        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity();
        
        for (String agentUid : physicalCapacity.getAgentUids()) {
            
            if (!restrictedAgentUids.contains(agentUid)) { 
                
                // machine matches isolation and zone SLA 
                AllocatedCapacity unallocatedCapacityOnAgent = 
                    physicalCapacity.getAgentCapacity(agentUid)
                    .subtract(usedCapacity.getAgentCapacityOrZero(agentUid));
                    
                unallocatedCapacity = 
                    unallocatedCapacity.add(agentUid, unallocatedCapacityOnAgent);
            }
        }
        
        return unallocatedCapacity;
    }

    /**
     * Calculates the total maximum capacity (memory/CPU) on all sla.getProvisionedAgents()
     */
    private AggregatedAllocatedCapacity getPhysicalProvisionedCapacity(AbstractMachinesSlaPolicy sla) {
        AggregatedAllocatedCapacity totalCapacity = new AggregatedAllocatedCapacity(); 
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

        NeedMoreCapacityException(AllocatedCapacity capacityShortage) {
            super("Cannot enforce Machines SLA since there are not enough machines available. "+
                  "Need more machines with " + capacityShortage);        
        }
    }
    
    private static class NeedMoreMachinesException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        NeedMoreMachinesException(int machineShortage) {
            super("Cannot enforce Containers SLA since there are not enough machines available. "+
                  "Need " + machineShortage + " more machines");
        }
    }
    
    private static class ScaleInObstructedException extends MachinesSlaEnforcementException {
        
        private static final long serialVersionUID = 1L;

        ScaleInObstructedException() {
            super("Cannot enforce Containers SLA since there are agents that need to be scaled in, but they are still running containers");
        }
    }

}


