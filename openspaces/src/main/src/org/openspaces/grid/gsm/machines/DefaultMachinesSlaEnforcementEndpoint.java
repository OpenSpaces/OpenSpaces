package org.openspaces.grid.gsm.machines;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

/**
 * Logic that decides whether to start a new machine machine or stop an existing machine
 * in order to enforce the specified SLA policy.
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
        
        if (sla.getMachineProvisioning() == null) {
            NonBlockingElasticMachineProvisioning defaultMachineProvisioning = getDefaultMachineProvisioningForProcessingUnit(pu,sla);
            sla.setMachineProvisioning(defaultMachineProvisioning);
        }
        
        if (sla.getContainerMemoryCapacityInMB() <= 0) {
            throw new IllegalArgumentException("Container memory capacity must be defined.");
        }
        
        try {
            return enforceSlaInternal(sla);
        } catch (ConflictingOperationInProgressException e) {
            logger.info("Cannot enforce Machines SLA since a conflicting operation is in progress. Try again later.", e);
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
        DefaultMachineProvisioning defaultMachineProvisioning = getDefaultMachineProvisioningForProcessingUnit(pu,sla);
        return enforceSlaInternal(sla, defaultMachineProvisioning);
    }

    private boolean enforceSlaInternal(EagerMachinesSlaPolicy sla, DefaultMachineProvisioning defaultMachineProvisioning) {

        cleanFutureAgents(pu, sla);
        cleanFailedMachines();
        cleanAgentsMarkedForDeallocation(defaultMachineProvisioning);

        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        AggregatedAllocatedCapacity capacityAllocated = state.getAllocatedCapacity(pu);
                
        if (state.getNumberOfFutureAgents(pu)>0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending deallocation.");
        }
        
        boolean slaReached = true;
        if (state.getNumberOfFutureAgents(pu)!=0 ||
            capacityMarkedForDeallocation.equalsZero()) {
            // operations are in progress. do not perform more operations in parallel
            slaReached = false;
        }
        else {
            // Eager scale out: ask for all available machines.
            int freeAgents = defaultMachineProvisioning.countFreeAgents();
            if (freeAgents > 0) {
                
                int numberOfMachines = capacityAllocated.getAgentUids().size();
                int machineShortage = Math.min(freeAgents, sla.getMaximumNumberOfMachines()-numberOfMachines);
                if (machineShortage > 0) {
                    FutureGridServiceAgents futureAgent = defaultMachineProvisioning.startMachinesAsync(
                        new CapacityRequirements(
                                new NumberOfMachinesCapacityRequirement(machineShortage)),
                        START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    state.futureAgent(pu,futureAgent);
                    
                    if (logger.isInfoEnabled()) {
                    logger.info(
                            machineShortage+ " new machine(s) is scheduled to be started "+
                            "in order to reach a total of " + (machineShortage+numberOfMachines) + " machines." +
                            "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
                    }
                    
                    // sla not reached until machine shortage is resolved.
                    slaReached = false;
                }
            }
        }
        
        if (state.getAllocatedCapacity(pu).getAgentUids().size() < sla.getMinimumNumberOfMachines()) {
            // number of agents does not meet SLA
            slaReached = false;
        }
        
        return slaReached;
    }
    
    public ProcessingUnit getId() {
        return pu;
    }
            
    private boolean enforceSlaInternal(CapacityMachinesSlaPolicy sla)
            throws ConflictingOperationInProgressException {

        cleanFutureAgents(pu, sla);
        cleanFailedMachines();
        cleanAgentsMarkedForDeallocation(sla.getMachineProvisioning());
        
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        AggregatedAllocatedCapacity capacityAllocated = state.getAllocatedCapacity(pu);
        
        if (state.getNumberOfFutureAgents(pu) > 0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending deallocation.");
        }
        
        boolean slaReached = 
            state.getNumberOfFutureAgents(pu)==0 && 
            capacityMarkedForDeallocation.equalsZero();
        
        
        
        AllocatedCapacity target =
            new AllocatedCapacity(
                    MachinesSlaUtils.convertCpuCoresFromDoubleToFraction(sla.getCpu()),
                    sla.getMemoryCapacityInMB());
        
        AggregatedAllocatedCapacity capacityAllocatedAndMarked = 
            AggregatedAllocatedCapacity.add(
                    capacityMarkedForDeallocation ,capacityAllocated);
        
        if (capacityAllocatedAndMarked.getTotalAllocatedCapacity().moreThanSatisfies(target) &&
            capacityAllocatedAndMarked.getAgentUids().size() > sla.getMinimumNumberOfMachines()) {
            
            logger.debug("Considering scale in: "+
                    "target is "+ target + " " +
                    "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                    "machines started " + state.getAllocatedCapacity(pu) + ", " + 
                    "machines pending deallocation " + state.getCapacityMarkedForDeallocation(pu));
            
            // scale in
            AllocatedCapacity surplusCapacity = AllocatedCapacity.subtract(
                    capacityAllocatedAndMarked.getTotalAllocatedCapacity(), target);           
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
                    surplusCapacity = AllocatedCapacity.subtract(surplusCapacity, agentCapacity);
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
                    surplusCapacity = AllocatedCapacity.subtract(surplusCapacity, agentCapacity);
                    surplusMachines --;
                    slaReached = false;
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
            
            int machineShortage = sla.getMinimumNumberOfMachines() - capacityAllocated.getAgentUids().size();
            
            if (state.getNumberOfFutureAgents(pu)==0) {
                // unmark machines pending deallocation and take into account with shortage calculation
                for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
                    if (machineShortage > 0) {
                    
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
            
            else if (capacityMarkedForDeallocation.equalsZero()) { 
                    
                // take into account expected machines into shortage calculate
                for (FutureGridServiceAgents future : state.getFutureAgents(pu)) {
                    
                    int expectedNumberOfMachines = future.getCapacityRequirements().getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
                    if (expectedNumberOfMachines == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    machineShortage -= expectedNumberOfMachines;
                }
            }
            else {
                throw new IllegalStateException("!capacityMarkedForDeallocation.equalsZero() && state.getNumberOfFutureAgents(pu)!=0. capacityMarkedForDeallocation=" + capacityMarkedForDeallocation + ", state.getNumberOfFutureAgents(pu)="+state.getNumberOfFutureAgents(pu));
            }
            
            if (machineShortage > 0) {
                
                startMachines(sla, machineShortage);
                slaReached = false;
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
                AllocatedCapacity.subtractOrZero(target,
                        capacityAllocatedAndMarked.getTotalAllocatedCapacity());
            
            // take into account expected machines into shortage calculate
            for (FutureGridServiceAgents future : state.getFutureAgents(pu)) {
                
                AllocatedCapacity expectedCapacity = MachinesSlaUtils.convertCapacityRequirementsToAllocatedCapacity(future.getCapacityRequirements());
                
                if (!shortageCapacity.isMemoryEqualsZero() && expectedCapacity.isMemoryEqualsZero()) {
                    // cannot determine expected memory, it could be enough to satisfy shortage
                    throw new ConflictingOperationInProgressException();
                }

                if (!shortageCapacity.isCpuCoresEqualsZero() && expectedCapacity.isCpuCoresEqualsZero()) {
                 // cannot determine expected cpu cores, it could be enough to satisfy shortage
                    throw new ConflictingOperationInProgressException();
                }
                
                shortageCapacity = 
                    AllocatedCapacity.subtractOrZero(shortageCapacity, expectedCapacity);
            }

           if (!shortageCapacity.equalsZero()) {
                slaReached = false;
                startMachines(sla, shortageCapacity);
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
        return slaReached;
    }

    private Collection<GridServiceAgent> sortManagementLast(Iterable<String> agentUids) {
        
        return MachinesSlaUtils.sortManagementLast(MachinesSlaUtils.getGridServiceAgentsFromUids(agentUids, pu.getAdmin()));
    }


    private void startMachines(CapacityMachinesSlaPolicy sla, AllocatedCapacity shortageCapacity) {
        
        final GridServiceAgent unusedManagementAgent = findUnusedManagementMachine(sla);    
        if (unusedManagementAgent != null) {
        
            AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(unusedManagementAgent,sla);
            state.allocateCapacity(pu, unusedManagementAgent.getUid(), agentCapacity);
            logger.info(
                    "Existing management machine " + MachinesSlaUtils.machineToString(unusedManagementAgent.getMachine()) + " " +
                    "is re-used to fill capacity shortage " + shortageCapacity + ", " + 
                    "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
        }
        else {
            
            FutureGridServiceAgents futureMachine = sla.getMachineProvisioning().startMachinesAsync(
                shortageCapacity.toCapacityRequirements(),
                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            state.futureAgent(pu, futureMachine);
            
            logger.info(
                    "One or more new machine(s) is started in order to "+
                    "fill capacity shortage " + shortageCapacity + 
                    "Allocated machine agents are: " + state.getAllocatedCapacity(pu) +" "+
                    "Pending future machine(s) requests " + state.getNumberOfFutureAgents(pu));
        }
        
    }

    private GridServiceAgent findUnusedManagementMachine(CapacityMachinesSlaPolicy sla) {
        GridServiceAgent unusedManagementMachine = null;
        final GridServiceAgent[] allUnusedManagementMachines = getAllUnusedManagementMachines();    
        if (sla.getAllowDeploymentOnManagementMachine() && 
            allUnusedManagementMachines.length > 0) {
        
            //special case where a management machine is already running and can be reused.
            unusedManagementMachine = allUnusedManagementMachines[0];
        }
        return unusedManagementMachine;
    }
    
    private void startMachines(CapacityMachinesSlaPolicy sla, int numberOfMachinesToStart) {
        final GridServiceAgent unusedManagementAgent = findUnusedManagementMachine(sla);    
        if (unusedManagementAgent != null) {
        
            AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(unusedManagementAgent,sla);
            state.allocateCapacity(pu, unusedManagementAgent.getUid(), agentCapacity);
            logger.info(
                    "Existing management machine " + MachinesSlaUtils.machineToString(unusedManagementAgent.getMachine()) + " " +
                    "is re-used to reach the minimum of " + 
                    sla.getMinimumNumberOfMachines() + " machines. " +
                    "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
        }
        else {    
            // scale out to get to the minimum number of agents
            state.futureAgent(pu,
                sla.getMachineProvisioning().startMachinesAsync(
                    new CapacityRequirements(
                            new NumberOfMachinesCapacityRequirement(numberOfMachinesToStart)),
                    START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            logger.info(
                numberOfMachinesToStart+ " new machine(s) is scheduled to be started in order to reach the minimum of " + 
                sla.getMinimumNumberOfMachines() + " machines. " +
                "Allocated machine agents are: " + state.getAllocatedCapacity(pu));
        }
    }

    private GridServiceAgent[] getAllUnusedManagementMachines() {
        final Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        for (final GridServiceAgent agent : pu.getAdmin().getGridServiceAgents()) {
            if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                agents.add(agent);
            }
        }
        
        agents.removeAll(state.getAllUsedAgentUids());
        return agents.toArray(new GridServiceAgent[agents.size()]);
    }
    
    /**
     * Kill agents marked for deallocation that no longer manage containers. 
     * @param machineProvisioning
     */
    private void cleanAgentsMarkedForDeallocation(NonBlockingElasticMachineProvisioning machineProvisioning) {
        
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        for (String agentUid : capacityMarkedForDeallocation.getAgentUids()) {
            
            AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agentUid);
            
            GridServiceAgent agent = pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid);
            if (agent == null) {
                logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is confirmed to be shutdown.");
                state.deallocateCapacity(pu, agentUid, agentCapacity);
            }
            else if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is running management processes but is considered deallocated for pu " + pu.getName());
                state.deallocateCapacity(pu, agentUid, agentCapacity);
            } 
            else {
                if (MachinesSlaUtils.getNumberOfChildProcesses(agent) == 0) {
                   // nothing running on this agent (not even GSM/LUS). Get rid of it.
                   logger.info(
                           "Stopping agent machine " + 
                           agent.getMachine().getHostAddress());
                   
                   machineProvisioning.stopMachineAsync(
                           agent, 
                           STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
                else if (MachinesSlaUtils.getNumberOfChildContainersForProcessingUnit(agent,pu) == 0) {
                    logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is no longer runnign containers for pu " + pu.getName() + " and is deallocated for that pu.");
                    state.deallocateCapacity(pu, agentUid, agentCapacity);    
                }
            }
        }
    }

    private void cleanFailedMachines() {
        for(String agentUid: state.getAllocatedCapacity(pu).getAgentUids()) {
            AllocatedCapacity agentCapacity = state.getAllocatedCapacity(pu).getAgentCapacity(agentUid);
            if (pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid) == null) {
                state.markCapacityForDeallocation(pu, agentUid, agentCapacity);
            }
        }
        
        for(String agentUid: state.getCapacityMarkedForDeallocation(pu).getAgentUids()) {
            AllocatedCapacity agentCapacity = state.getCapacityMarkedForDeallocation(pu).getAgentCapacity(agentUid);
            if (pu.getAdmin().getGridServiceAgents().getAgentByUID(agentUid) == null) {
                state.deallocateCapacity(pu, agentUid, agentCapacity);
            }
        }
    }
    
    /**
     * Move future agents that completed startup, from the futureAgents list to the agentsStarted list. 
     */
    private void cleanFutureAgents(ProcessingUnit pu, AbstractMachinesSlaPolicy sla) {
        
        for (FutureGridServiceAgents future : state.removeAllDoneFutureAgents(pu)) {
            
            Throwable exception = null;
            try {
            
                // create a set of new agents
                // throws an exception if anything went wrong.
                GridServiceAgent[] newAgents = filterNewAgentsIfAlreadyInUse(future.get());
                warnAboutAgentsWithWrongMachineZone(sla, newAgents);
                //update started agents list with the list of new agents
                for (GridServiceAgent agent : newAgents) {
                    //TODO: If pu isolation is public or shared then need to allocate only part of the machine's capacity. And not the maximum capacity.
                    AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(agent,sla);
                    state.allocateCapacity(pu, agent.getUid(), agentCapacity);
                    if (logger.isInfoEnabled()) {
                        logger.info("Agent started succesfully on a new machine " + agent.getMachine().getHostAddress());
                    }
                }
                
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
        }
        
    }
    

    private GridServiceAgent[] filterNewAgentsIfAlreadyInUse(GridServiceAgent[] newAgents) {
        
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>(Arrays.asList(newAgents));
        
        final Set<String> usedAgentUids = state.getAllUsedAgentUids();
        final Iterator<GridServiceAgent> iterator = agents.iterator();
        while(iterator.hasNext()) {

            GridServiceAgent agent = iterator.next();
            if (usedAgentUids.contains(agent.getUid())) {
                // remove the new agent since it is already being used
                iterator.remove();
                
                if (logger.isWarnEnabled()) {
                        logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the machine " + agent.getMachine().getHostAddress() +
                            " which is already in use."+
                            "This violating machine has been ignored");
                }
            }
        }
        
        return agents.toArray(new GridServiceAgent[agents.size()]);
    }
    
    private void warnAboutAgentsWithWrongMachineZone(AbstractMachinesSlaPolicy sla, GridServiceAgent[] agents) {
        if (logger.isWarnEnabled()) {
            for (GridServiceAgent agent : agents) {
                if (!MachinesSlaUtils.matchesMachineZones(sla, agent)) {
                    logger.warn(
                        "Machine provisioning for " + pu.getName() + " "+
                        "has provided the machine " + agent.getMachine().getHostAddress() + ", which has a wrong zone."+
                        "This violation is ignored, and the machine will be used.");
                }
            }
        }

    }

    /**
     * This is the default machine provisioning implementation which is tightly coupled
     * with the state hold by the various endpoints. It reads and modifies the shared state used by all endpoints.
     * 
     * It basically looks for agents that are not used by any endpoint and returns it wrapped as a FutureMachine.
     *  
     * @return
     */
    private DefaultMachineProvisioning getDefaultMachineProvisioningForProcessingUnit(ProcessingUnit pu, AbstractMachinesSlaPolicy sla) {
        return new DefaultMachineProvisioning(pu, sla, state);
    }

    @SuppressWarnings("serial")
    private static class ConflictingOperationInProgressException extends Exception  {}


}


