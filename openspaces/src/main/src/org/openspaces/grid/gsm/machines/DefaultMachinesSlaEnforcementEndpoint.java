package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    
    public GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
       
        Collection<GridServiceAgent> agentsStarted = state.getCapacityAllocated(pu).getAgents();
        return agentsStarted.toArray(new GridServiceAgent[agentsStarted.size()]);
    }

     
    public GridServiceAgent[] getGridServiceAgentsPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateEndpointNotDestroyed(pu);
        
        Collection<GridServiceAgent> agentsPendingShutdown = state.getCapacityMarkedForDeallocation(pu).getAgents();
        return agentsPendingShutdown.toArray(new GridServiceAgent[agentsPendingShutdown.size()]);
        
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
        DefaultMachineProvisioning defaultMachineProvisioning = getDefaultMachineProvisioningForProcessingUnit(pu,sla);
        return enforceSlaInternal(sla, defaultMachineProvisioning);
    }

    private boolean enforceSlaInternal(EagerMachinesSlaPolicy sla, DefaultMachineProvisioning defaultMachineProvisioning) {

        cleanFutureAgents(pu, sla);
        cleanFailedMachines();
        cleanAgentsMarkedForShutdown(defaultMachineProvisioning);

        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        AggregatedAllocatedCapacity capacityAllocated = state.getCapacityAllocated(pu);
        
        AggregatedAllocatedCapacity capacityAllocatedAndMarked = AggregatedAllocatedCapacity.add(capacityMarkedForDeallocation, capacityAllocated);
        
        if (state.getNumberOfFutureAgents(pu)>0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending shutdown.");
        }
        
        boolean slaReached = 
            state.getNumberOfFutureAgents(pu)==0 &&
            capacityMarkedForDeallocation.equalsZero();
        
        // Eager more: we ask for all available machines.
        int maxNumberOfMachines = Math.max(sla.getMinimumNumberOfMachines(), pu.getTotalNumberOfInstances());

        if (capacityAllocatedAndMarked.getAgents().size() > maxNumberOfMachines) {
                
            logger.info("Considering scale in: "+
                    "max #machines is " + maxNumberOfMachines + ", " +
                    "existing #machines started " + capacityAllocated + ", " + 
                    "existing #machines pending shutdown " + capacityMarkedForDeallocation);
            
            // scale in
            int surplusMachines = 
                capacityAllocatedAndMarked.getAgents().size()  - maxNumberOfMachines;
            
            // adjust surplusMachines based on agents marked for shutdown
            for (GridServiceAgent agent : capacityMarkedForDeallocation.getAgents()) {

                if (surplusMachines > 0) {
                    // this machine is already marked for shutdown, so surplus
                    // is adjusted to reflect that
                    surplusMachines--;
                } else {
                    // cancel scale in
                    state.unmarkCapacityForDeallocation(
                            pu, 
                            agent, 
                            capacityMarkedForDeallocation.getAgentCapacity(agent));
                    
                    logger.info(
                            "machine agent " + agent.getMachine().getHostAddress() + " " +
                            "is no longer marked for shutdown in order to maintain capacity. "+
                            "Approved machine agents are: " + state.getCapacityAllocated(pu));
                }
            }

            // mark agents for shutdown if there are enough of them (scale in)
            // give priority to agents that do not host a GSM/LUS since we want to evacuate those last.
            for (GridServiceAgent agent : MachinesSlaUtils.sortManagementLast(capacityAllocated.getAgents())) {
                if (surplusMachines > 0) {
                    // mark machine for shutdown
                    state.markCapacityForDeallocation(pu, agent, capacityAllocated.getAgentCapacity(agent));
                    surplusMachines --;
                    slaReached = false;
                    logger.info(
                            "Machine agent " + agent.getMachine().getHostAddress() + " is marked for shutdown in order to reduce capacity. "+
                            "Approved machine agents are: " + state.getCapacityAllocated(pu));
                }
            }
        }
        else if (state.getNumberOfFutureAgents(pu)==0 && 
                 capacityMarkedForDeallocation.getAgents().isEmpty() &&
                 capacityAllocated.getAgents().size() < maxNumberOfMachines){
            
            int freeAgents = defaultMachineProvisioning.countFreeAgents();
            if (freeAgents > 0) {
                int machineShortage = Math.min(freeAgents, maxNumberOfMachines-capacityAllocated.getAgents().size());
                FutureGridServiceAgents futureAgent = defaultMachineProvisioning.startMachinesAsync(
                    new CapacityRequirements(
                            new NumberOfMachinesCapacityRequirement(machineShortage)),
                    START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                state.futureAgent(pu,futureAgent);
                slaReached = false;
                if (logger.isInfoEnabled()) {
                logger.info(
                        machineShortage+ " new machine(s) is scheduled to be started "+
                        "in order to reach a total of " + maxNumberOfMachines + " machines." +
                        "Approved machine agents are: " + state.getCapacityAllocated(pu));
                }
            }
        }
        
        if (getGridServiceAgents().length < sla.getMinimumNumberOfMachines()) {
            slaReached = false;
        }
        
        return slaReached;
    }
    
    public long getMemoryCapacityInMB(EagerMachinesSlaPolicy sla) {
        long memoryInMB = 0;
        for (GridServiceAgent agent : getGridServiceAgents()) {
            memoryInMB += MachinesSlaUtils.getMemoryInMB(agent.getMachine(), sla);
        }
        return memoryInMB;
    }

    public ProcessingUnit getId() {
        return pu;
    }

            
    private boolean enforceSlaInternal(CapacityMachinesSlaPolicy sla)
            throws ConflictingOperationInProgressException {

        cleanFutureAgents(pu, sla);
        cleanFailedMachines();
        cleanAgentsMarkedForShutdown(sla.getMachineProvisioning());
        
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        AggregatedAllocatedCapacity capacityAllocated = state.getCapacityAllocated(pu);
        
        if (state.getNumberOfFutureAgents(pu) > 0 && 
            !capacityMarkedForDeallocation.equalsZero()) {
            throw new IllegalStateException("Cannot have both agents pending to be started and agents pending shutdown.");
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
        
        if (capacityAllocatedAndMarked.getTotalAllocatedCapacity().biggerThan(target) &&
            capacityAllocatedAndMarked.getAgents().size() > sla.getMinimumNumberOfMachines()) {
            
            logger.debug("Considering scale in: "+
                    "target is "+ target + " " +
                    "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                    "machines started " + state.getCapacityAllocated(pu) + ", " + 
                    "machines pending shutdown " + state.getCapacityMarkedForDeallocation(pu));
            
            // scale in
            AllocatedCapacity surplusCapacity = AllocatedCapacity.subtract(
                    capacityAllocatedAndMarked.getTotalAllocatedCapacity(), target);           
            int surplusMachines = capacityAllocatedAndMarked.getAgents().size() - sla.getMinimumNumberOfMachines();
            
            // adjust surplusMemory based on agents marked for shutdown
            // remove mark if it would cause surplus to be below zero
            // remove mark if it would reduce the number of machines below the sla minimum.
            for (GridServiceAgent agent : capacityMarkedForDeallocation.getAgents()) {

                AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agent);
                if (surplusCapacity.equalsOrBiggerThan(agentCapacity) &&
                    surplusMachines > 0) {
                    // this machine is already marked for shutdown, so surplus
                    // is adjusted to reflect that
                    surplusCapacity = AllocatedCapacity.subtract(surplusCapacity, agentCapacity);
                    surplusMachines--;
                } else {
                    // cancel scale in
                    state.unmarkCapacityForDeallocation(pu, agent, agentCapacity);
                    logger.info(
                            "machine agent " + agent.getMachine().getHostAddress() + " " +
                            "is no longer marked for shutdown in order to maintain capacity. "+
                            "Approved machine agents are: " + state.getCapacityAllocated(pu));
                }
            }

            // mark agents for shutdown if there are not enough of them (scale in)
            // give priority to agents that do not host a GSM/LUS since we want to evacuate those last.
            for (GridServiceAgent agent : MachinesSlaUtils.sortManagementLast(capacityAllocated.getAgents())) {
                AllocatedCapacity agentCapacity = capacityAllocated.getAgentCapacity(agent);
                
                if (surplusCapacity.equalsOrBiggerThan(agentCapacity) &&
                    surplusMachines > 0) {

                    // scale in machine
                    state.markCapacityForDeallocation(pu, agent, agentCapacity);
                    surplusCapacity = AllocatedCapacity.subtract(surplusCapacity, agentCapacity);
                    surplusMachines --;
                    slaReached = false;
                    logger.info(
                            "Machine agent " + agent.getMachine().getHostAddress() + " is marked for shutdown in order to reduce capacity. "+
                            "Approved machine agents are: " + state.getCapacityAllocated(pu));
                }
            }
        }

        else if (capacityAllocated.getAgents().size() <  sla.getMinimumNumberOfMachines()) {
            
            logger.info("Considering to start more machines to reach required minimum number of machines: " + 
                    capacityAllocated + " started, " +
                    capacityMarkedForDeallocation + " marked for shutdown, " +
                    sla.getMinimumNumberOfMachines() + " is the required minimum number of machines."
            );
            
            int machineShortage = sla.getMinimumNumberOfMachines() - capacityAllocated.getAgents().size();
            
            if (state.getNumberOfFutureAgents(pu)==0) {
                // unmark machines pending deallocation and take into account with shortage calculation
                for (GridServiceAgent agent : capacityMarkedForDeallocation.getAgents()) {
                    if (machineShortage > 0) {
                    
                        AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agent);
                        state.unmarkCapacityForDeallocation(pu, agent, agentCapacity);
                        machineShortage--;
                    
                        if (logger.isInfoEnabled()) {
                            logger.info(
                                    "machine " + MachinesSlaUtils.machineToString(agent.getMachine()) + " "+
                                    "is no longer marked for shutdown in order to reach the minimum of " + 
                                    sla.getMinimumNumberOfMachines() + " machines.");
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
        
        else if (!capacityAllocatedAndMarked.getTotalAllocatedCapacity().equalsOrBiggerThan(target)) {
            
            // scale out

            if (logger.isInfoEnabled()) {
            logger.info("Considering to start more machines inorder to reach target capacity:" + 
                    "target is "+ target +
                    "machines started " + state.getCapacityAllocated(pu) + ", " + 
                    "machines pending shutdown " + state.getCapacityMarkedForDeallocation(pu));
            }
            
            // unmark all machines pending shutdown              
            for (GridServiceAgent agent : capacityMarkedForDeallocation.getAgents()) {
                AllocatedCapacity agentCapacity =  capacityMarkedForDeallocation.getAgentCapacity(agent);
                logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
                state.unmarkCapacityForDeallocation(pu, agent, agentCapacity);
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
                    "marked for shutdown="+capacityMarkedForDeallocation + ", " +
                    "#futures="+state.getNumberOfFutureAgents(pu) + " " +
                    "#minimumMachines="+sla.getMinimumNumberOfMachines());        
        }
        return slaReached;
    }

    private void startMachines(CapacityMachinesSlaPolicy sla, AllocatedCapacity shortageCapacity) {
        
        final GridServiceAgent unusedManagementAgent = findUnusedManagementMachine(sla);    
        if (unusedManagementAgent != null) {
        
            AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(unusedManagementAgent,sla);
            state.allocateCapacity(pu, unusedManagementAgent, agentCapacity);
            logger.info(
                    "Existing management machine " + MachinesSlaUtils.machineToString(unusedManagementAgent.getMachine()) + " " +
                    "is re-used to fill capacity shortage " + shortageCapacity + ", " + 
                    "Approved machine agents are: " + state.getCapacityAllocated(pu));
        }
        else {
            
            FutureGridServiceAgents futureMachine = sla.getMachineProvisioning().startMachinesAsync(
                shortageCapacity.toCapacityRequirements(),
                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            state.futureAgent(pu, futureMachine);
            
            logger.info(
                    "One or more new machine(s) is started in order to "+
                    "fill capacity shortage " + shortageCapacity + 
                    "Approved machine agents are: " + state.getCapacityAllocated(pu) +" "+
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
            state.allocateCapacity(pu, unusedManagementAgent, agentCapacity);
            logger.info(
                    "Existing management machine " + MachinesSlaUtils.machineToString(unusedManagementAgent.getMachine()) + " " +
                    "is re-used to reach the minimum of " + 
                    sla.getMinimumNumberOfMachines() + " machines. " +
                    "Approved machine agents are: " + state.getCapacityAllocated(pu));
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
                "Approved machine agents are: " + state.getCapacityAllocated(pu));
        }
    }

    private GridServiceAgent[] getAllUnusedManagementMachines() {
        final Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        for (final GridServiceAgent agent : pu.getAdmin().getGridServiceAgents()) {
            if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                agents.add(agent);
            }
        }
        
        agents.removeAll(state.getAllUsedAgents());
        return agents.toArray(new GridServiceAgent[agents.size()]);
    }
    
    /**
     * Kill agents marked for shutdown that no longer manage containers. 
     * @param machineProvisioning
     */
    private void cleanAgentsMarkedForShutdown(NonBlockingElasticMachineProvisioning machineProvisioning) {
        
        AggregatedAllocatedCapacity capacityMarkedForDeallocation = state.getCapacityMarkedForDeallocation(pu);
        for (GridServiceAgent agent : capacityMarkedForDeallocation.getAgents()) {
            
            AllocatedCapacity agentCapacity = capacityMarkedForDeallocation.getAgentCapacity(agent);
            
            int numberOfChildProcesses = MachinesSlaUtils.getNumberOfChildProcesses(agent);
            
            if (!agent.isDiscovered()) {
                logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is confirmed to be shutdown.");
                state.deallocateCapacity(pu, agent, agentCapacity);
            } 
            else if (MachinesSlaUtils.isManagementRunningOnMachine(agent.getMachine())) {
                logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is running management processes but is not needed for pu " + pu.getName());
                state.deallocateCapacity(pu, agent, agentCapacity);
            }
            else if (numberOfChildProcesses == 0) {
               // nothing running on this agent (not even GSM/LUS). Get rid of it.
               logger.info(
                       "Stopping agent machine " + 
                       agent.getMachine().getHostAddress());
               
               machineProvisioning.stopMachineAsync(
                       agent, 
                       STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void cleanFailedMachines() {
        AggregatedAllocatedCapacity capacityAllocated = state.getCapacityAllocated(pu);
        
        for(GridServiceAgent agent : capacityAllocated.getAgents()) {
            AllocatedCapacity agentCapacity = capacityAllocated.getAgentCapacity(agent);
            if (!agent.isDiscovered()) {
                state.markCapacityForDeallocation(pu, agent, agentCapacity);
                state.deallocateCapacity(pu, agent, agentCapacity);
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
                Set<GridServiceAgent> newAgents = new HashSet<GridServiceAgent>(Arrays.asList(
                                    future.get()));
                
                // create a set of agents that are both in the new and already started agents.
                Set<GridServiceAgent> duplicateAgents = state.getAllUsedAgents();
                duplicateAgents.retainAll(newAgents);
                
                // the duplicate list should be empty.
                if (!duplicateAgents.isEmpty()) {
                    
                    // remove all new agents that are duplicate
                    newAgents.remove(duplicateAgents);
                    
                    if (logger.isWarnEnabled()) {
                        List<String> usedMachines = new ArrayList<String>();
                        for (GridServiceAgent usedAgent : duplicateAgents) {
                            usedMachines.add(usedAgent.getMachine().getHostAddress());
                        }
                        
                        logger.warn(
                                "Machine provisioning for " + pu.getName() + " "+
                                "has provided the following machines, which are already in use: "+
                                Arrays.toString(usedMachines.toArray(new String[usedMachines.size()])) +
                                "These violating machines have been ignored, "+
                                "but it should not have happened in the first place.");
                    }
                }
                
                List<String> machinesWrongZone = new ArrayList<String>();
                for (GridServiceAgent agent : newAgents) {
                    if (!MachinesSlaUtils.matchesMachineZones(sla, agent)) {
                        machinesWrongZone.add(agent.getMachine().getHostAddress());
                    }
                }
                if (!machinesWrongZone.isEmpty()) {
                    
                    if (logger.isWarnEnabled()) {
                        logger.warn(
                            "Machine provisioning for " + pu.getName() + " "+
                            "has provided the following machines, which have the wrong zone: "+
                            Arrays.toString(machinesWrongZone.toArray(new String[machinesWrongZone.size()])) +
                            "These violating machines are will be used, "+
                            "but it should not have happened in the first place.");
                    }
                }
                
                //update started agents list with the list of new agents
                for (GridServiceAgent agent : newAgents) {
                    AllocatedCapacity agentCapacity = MachinesSlaUtils.getMaxAllocatedCapacity(agent,sla);
                    state.allocateCapacity(pu, agent, agentCapacity);
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


