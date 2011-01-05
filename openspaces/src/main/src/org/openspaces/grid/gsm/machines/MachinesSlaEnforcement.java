package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class MachinesSlaEnforcement implements
        ServiceLevelAgreementEnforcement<MachinesSlaPolicy, ProcessingUnit, MachinesSlaEnforcementEndpoint> {

    private static final Log logger = LogFactory.getLog(MachinesSlaEnforcementEndpoint.class);
    private static final int START_AGENT_TIMEOUT_SECONDS = 10*60;
    private static final long STOP_AGENT_TIMEOUT_SECONDS = 10*60;

    // state that tracks managed grid service agents, agents to be started and agents marked for shutdown.
    private final Map<ProcessingUnit,List<GridServiceAgent>> agentsStartedPerProcessingUnit;
    private final Map<ProcessingUnit,List<FutureGridServiceAgents>> futureAgentsPerProcessingUnit;
    private final Map<ProcessingUnit,List<GridServiceAgent>> agentsPendingShutdownPerProcessingUnit;
    
    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;

    private final Admin admin;

    public MachinesSlaEnforcement(Admin admin) {

        endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        agentsStartedPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceAgent>>();
        futureAgentsPerProcessingUnit = new HashMap<ProcessingUnit, List<FutureGridServiceAgents>>();
        agentsPendingShutdownPerProcessingUnit = new HashMap<ProcessingUnit, List<GridServiceAgent>>();
        
        this.admin = admin;
    }

    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        
        if (!isEndpointDestroyed(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("PU has to have exactly 1 zone defined");
        }

        String zone = pu.getRequiredZones()[0];
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            
            // found a container associated with this pu. take it.
            if (container.getZones().size() == 1 &&
                container.getZones().containsKey(zone) &&
                container.getMachine().getGridServiceAgents().getSize() == 1) {

                agents.add(container.getMachine().getGridServiceAgent());
           }
        }

        for (MachinesSlaEnforcementEndpoint endpoint : endpoints.values()) {
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgents()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgentsPendingShutdown()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
        }

        agentsStartedPerProcessingUnit.put(pu,new ArrayList<GridServiceAgent>(agents));
        futureAgentsPerProcessingUnit.put(pu, new ArrayList<FutureGridServiceAgents>());
        agentsPendingShutdownPerProcessingUnit.put(pu, new ArrayList<GridServiceAgent>());
        
        MachinesSlaEnforcementEndpoint endpoint = new DefaultMachinesSlaEnforcementEndpoint(pu);
        endpoints.put(pu, endpoint);
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        endpoints.remove(pu);
        agentsStartedPerProcessingUnit.remove(pu);
        futureAgentsPerProcessingUnit.remove(pu);
        agentsPendingShutdownPerProcessingUnit.remove(pu);
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
    
    private void validateEndpointNotDestroyed(ProcessingUnit pu) {
        if (isEndpointDestroyed(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }
    
    private boolean isEndpointDestroyed(ProcessingUnit pu) {
        return 
            endpoints.get(pu) == null ||
            agentsStartedPerProcessingUnit.get(pu) == null ||
            futureAgentsPerProcessingUnit.get(pu) == null ||
            agentsPendingShutdownPerProcessingUnit.get(pu) == null;
    }
    
    private Set<GridServiceAgent> getAllUsedGridServiceAgents() {
        
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        for (MachinesSlaEnforcementEndpoint endpoint : endpoints.values()) {
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgents()) {
                agents.add(gsa);
            }
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgentsPendingShutdown()) {
                agents.add(gsa);
            }
        }
        
        return agents;
    }
    
    /**
     * Logic that decides werther to start a new machine machine or stop an existing machine
     * based on the specified SLA policy.
     * 
     * @author itaif
     * @see MachinesSlaEnforcement - creates this endpoint
     * @see MachinesSlaPolicy - defines the sla policy for this endpoint
     */
    class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint {

        private final ProcessingUnit pu;
        
        public DefaultMachinesSlaEnforcementEndpoint(ProcessingUnit pu) {
            
            if (pu == null) {
                throw new IllegalArgumentException("pu cannot be null.");
            }
            
            this.pu = pu;           
        }
        
        public GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateEndpointNotDestroyed(pu);
           
            List<GridServiceAgent> agentsStarted = getAgentsStarted();
            return agentsStarted.toArray(new GridServiceAgent[agentsStarted.size()]);
        }

         
        public GridServiceAgent[] getGridServiceAgentsPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateEndpointNotDestroyed(pu);
            
            List<GridServiceAgent> agentsPendingShutdown = getAgentsPendingShutdown();
            return agentsPendingShutdown.toArray(new GridServiceAgent[agentsPendingShutdown.size()]);
            
        }

        public boolean enforceSla(MachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
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
                sla.setMachineProvisioning(getDefaultMachineProvisioningForProcessingUnit(pu));
            }
            
            try {
                return enforceSlaInternal(sla);
            } catch (ConflictingOperationInProgressException e) {
                logger.info("Cannot enforce Machines SLA since a conflicting operation is in progress. Try again later.", e);
                return false; // try again next time
            }
        }
        
        public ProcessingUnit getId() {
            return pu;
        }

                
        private boolean enforceSlaInternal(MachinesSlaPolicy sla)
                throws ConflictingOperationInProgressException {

            cleanAgentsMarkedForShutdown(sla.getMachineProvisioning());
            cleanFutureAgents();

            if (!getFutureAgents().isEmpty() && !getAgentsPendingShutdown().isEmpty()) {
                throw new IllegalStateException("Cannot have both agents pending to be started and agents pending shutdown.");
            }
            
            boolean slaReached = getFutureAgents().isEmpty() && getAgentsPendingShutdown().isEmpty();
            
            long targetMemory = sla.getMemoryCapacityInMB();
            double targetCpu = sla.getCpu();

            int existingMemory = 0;
            double existingCpu = 0;
            
            for (GridServiceAgent agent : getAgentsStarted()) {
                existingMemory += MachinesSlaUtils.getMemoryInMB(agent);
                existingCpu += MachinesSlaUtils.getCpu(agent);
            }
            
            for (GridServiceAgent agent : getAgentsPendingShutdown()) {
                existingMemory += MachinesSlaUtils.getMemoryInMB(agent);
                existingCpu += MachinesSlaUtils.getCpu(agent);
            }

            if (existingMemory > targetMemory && 
                existingCpu > targetCpu && 
                getAgentsStarted().size() + getAgentsPendingShutdown().size() > sla.getMinimumNumberOfMachines()) {
                
                logger.debug("Considering scale in: "+
                        "target memory is "+ targetMemory +"MB, "+
                        "existing memory is " + existingMemory +"MB, "+
                        "target CPU is " + targetCpu + " " +
                        "existing CPU is " + existingCpu + ", " +
                        "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                        "existing #machines is " + getAgentsStarted().size() + getAgentsPendingShutdown().size());
                
                // scale in
                long surplusMemory = existingMemory - targetMemory;
                double surplusCpu = existingCpu - targetCpu;
                
                int surplusMachines = getAgentsStarted().size() + getAgentsPendingShutdown().size() - sla.getMinimumNumberOfMachines();
                
                // adjust surplusMemory based on agents marked for shutdown
                // remove mark if it would cause surplus to be below zero
                // remove mark if it would reduce the number of machines below the sla minimum.
                Iterator<GridServiceAgent> iterator = Arrays.asList(
                        getGridServiceAgentsPendingShutdown()).iterator();
                while (iterator.hasNext()) {

                    GridServiceAgent agent = iterator.next();
                    int machineMemory = MachinesSlaUtils.getMemoryInMB(agent);
                    double machineCpu = MachinesSlaUtils.getCpu(agent);
                    if (surplusMemory >= machineMemory &&
                        surplusCpu >= machineCpu &&
                        surplusMachines > 0) {
                        // this machine is already marked for shutdown, so surplus
                        // is adjusted to reflect that
                        surplusMemory -= machineMemory;
                        surplusCpu -= machineCpu;
                        surplusMachines--;
                    } else {
                        iterator.remove();
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
                    }
                }

                // mark agents for shutdown if there are not enough of them (scale in)
                // give priority to agents that do not host a GSM/LUS since we want to evacuate those last.
                for (GridServiceAgent agent : getGridServiceAgents()) {
                    int machineMemory = MachinesSlaUtils.getMemoryInMB(agent);
                    double machineCpu = MachinesSlaUtils.getCpu(agent);
                    if (surplusMemory >= machineMemory && surplusCpu >= machineCpu && surplusMachines > 0) {

                        // mark machine for shutdown unless it is a management
                        // machine
                        this.getAgentsPendingShutdown().add(agent);
                        this.getAgentsStarted().remove(agent);
                        surplusMemory -= machineMemory;
                        surplusCpu -= machineCpu;
                        surplusMachines --;
                        slaReached = false;
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is marked for shutdown in order to reduce capacity.");
                    }
                }
            }

            else if (getFutureAgents().isEmpty() && 
                    (getAgentsStarted().size() - getAgentsPendingShutdown().size() < sla.getMinimumNumberOfMachines())) {
                
                logger.info("Considering to start more machines to reach required minimum number of machines: " + 
                            getAgentsStarted().size() + " machine agents started, " +
                            getAgentsPendingShutdown().size() + " machine agents marked for shutdown, " +
                            sla.getMinimumNumberOfMachines() + " is the required minimum number of machines."
                );
                int machineShortage = sla.getMinimumNumberOfMachines() + getAgentsPendingShutdown().size() 
                                      - getAgentsStarted().size();
                
                for (int i =0 ; i < machineShortage && getAgentsPendingShutdown().size() > 0; i++) {
                    GridServiceAgent agent = getAgentsPendingShutdown().remove(0);
                    logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to reach the minimum of " + sla.getMinimumNumberOfMachines() + " machines.");
                }
                
                machineShortage = sla.getMinimumNumberOfMachines() + getAgentsPendingShutdown().size() 
                                    - getAgentsStarted().size();
                if (machineShortage > 0) {
                    this.getFutureAgents().add(
                            sla.getMachineProvisioning().startMachinesAsync(
                                new CapacityRequirements(
                                        new NumberOfMachinesCapacityRequirement(machineShortage)),
                                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info(machineShortage+ " new machine(s) is scheduled to be started in order to reach the minimum of " + sla.getMinimumNumberOfMachines() + " machines.");
                }
            }
            
            else if (getAgentsPendingShutdown().isEmpty() && 
                    (getAgentsStarted().size()  < sla.getMinimumNumberOfMachines())) {
                
                int machineShortage = sla.getMinimumNumberOfMachines() - getAgentsStarted().size();
                
             // take into account expected machines into shortage calculate
                for (FutureGridServiceAgents future : this.getFutureAgents()) {
                    
                    int expectedNumberOfMachines = future.getCapacityRequirements().getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
                    if (expectedNumberOfMachines == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    machineShortage -= expectedNumberOfMachines;
                }

                if (machineShortage > 0) {
                
                    // scale out to get to the minimum number of agents
                     
                    this.getFutureAgents().add(
                            sla.getMachineProvisioning().startMachinesAsync(
                                new CapacityRequirements(
                                        new NumberOfMachinesCapacityRequirement(machineShortage)),
                                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info(machineShortage + " " + 
                            "new machine(s) is scheduled to be started in order to "+
                            "reach the minimum of " + sla.getMinimumNumberOfMachines() + " " +
                            "machines.");
                }
            }
            
            else if (existingMemory < targetMemory || existingCpu < targetCpu) {
                // scale out

                logger.info("Considering to start more machines inorder to reach target capacity:" + 
                        "target memory is "+ targetMemory +"MB, "+
                        "existing memory is " + existingMemory +"MB, "+
                        "target CPU is " + targetCpu + " " +
                        "existing CPU is " + existingCpu);
                
                // unmark all machines pending shutdown
                for (GridServiceAgent agent : getAgentsPendingShutdown()) {
                    logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
                }
                this.getAgentsPendingShutdown().clear();
                    

                long shortageMemory = targetMemory - existingMemory;
                double shortageCpu = targetCpu - existingCpu;

                // take into account expected machines into shortage calculate
                for (FutureGridServiceAgents future : this.getFutureAgents()) {
                    
                    long expectedMachineMemory = future.getCapacityRequirements().getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
                    if (shortageMemory > 0 && expectedMachineMemory == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    shortageMemory -= expectedMachineMemory;

                    double expectedMachineCpu = future.getCapacityRequirements().getRequirement(CpuCapacityRequirement.class).getCpu();
                    if (shortageCpu > 0 && expectedMachineCpu == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    shortageCpu -= expectedMachineCpu;
                }

                if (shortageMemory < 0) {
                    shortageMemory = 0;
                }
                if (shortageCpu < 0) {
                    shortageCpu = 0;
                }

                if (shortageCpu >0 || shortageMemory > 0) {
                    slaReached = false;
                
                    
                    this.getFutureAgents().add(
                        sla.getMachineProvisioning().startMachinesAsync(
                            new CapacityRequirements(
                                    new MemoryCapacityRequirment(shortageMemory),
                                    new CpuCapacityRequirement(shortageCpu)),
                            START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info("One or more new machine were scheduled to be started in order to increase capacity.");
                }
            }
            else {
                logger.debug("No action required in order to enforce machines sla.");
            }
            return slaReached;
        }

        private void cleanAgentsMarkedForShutdown(NonBlockingElasticMachineProvisioning machineProvisioning) {
            
            for (GridServiceAgent agent : 
                    new ArrayList<GridServiceAgent>(getAgentsPendingShutdown())) {
                
                int numberOfChildProcesses = MachinesSlaUtils.getNumberOfChildProcesses(agent);
                
                if (!agent.isDiscovered()) {
                    logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is confirmed to be shutdown.");
                    getAgentsPendingShutdown().remove(agent);
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

        private void cleanFutureAgents() {
            final Iterator<FutureGridServiceAgents> iterator = getFutureAgents().iterator();
            while (iterator.hasNext()) {
                FutureGridServiceAgents future = iterator.next();
                
                if (future.isDone()) {
                
                    iterator.remove();
                    
                    Throwable exception = null;
                    try {
                    
                        Set<GridServiceAgent> newAgents = 
                            new HashSet<GridServiceAgent>(
                                    Arrays.asList(
                                            future.get()));
                        
                        Set<GridServiceAgent> usedAgents = getAllUsedGridServiceAgents();
                        usedAgents.retainAll(newAgents);
                        if (!usedAgents.isEmpty()) {
                            
                            // remove all agents that are already in use
                            newAgents.remove(usedAgents);
                            List<String> usedMachines = new ArrayList<String>();
                            for (GridServiceAgent usedAgent : usedAgents) {
                                usedMachines.add(usedAgent.getMachine().getHostAddress());
                            }
                            
                            logger.warn(
                                    "Machine provisioning for " + pu.getName() + " "+
                                    "has provided the following machines, which are already in use: "+
                                    Arrays.toString(usedMachines.toArray(new String[usedMachines.size()])) +
                                    "This violating machines have been ignored, but it should not have happened in the first place."); 
                        }
                        
                        getAgentsStarted().addAll(newAgents);
                        if (logger.isInfoEnabled()) {
                            for (GridServiceAgent agent : newAgents) {
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
                        logger.warn(errorMessage , exception);
                    }
                }
            }
            
        }
        
        public List<GridServiceAgent> getAgentsStarted() {
            return agentsStartedPerProcessingUnit.get(pu);
        }

        public List<FutureGridServiceAgents> getFutureAgents() {
            return futureAgentsPerProcessingUnit.get(pu);
        }

        public List<GridServiceAgent> getAgentsPendingShutdown() {
            return agentsPendingShutdownPerProcessingUnit.get(pu);
        }
    }
    

    @SuppressWarnings("serial")
    private static class ConflictingOperationInProgressException extends Exception  {}

    /**
     * This is the default machine provisioning implementation which is tightly coupled
     * with the state hold by the various endpoints.
     * 
     * It provides a PollingFuture which lazily allocates the required capacity when
     * polled for the first time.
     * 
     * @return
     */
    private NonBlockingElasticMachineProvisioning getDefaultMachineProvisioningForProcessingUnit(ProcessingUnit pu) {
        return new DefaultMachineProvisioning(pu);
    }
    
    class DefaultMachineProvisioning implements NonBlockingElasticMachineProvisioning {

        private final ProcessingUnit pu;
        DefaultMachineProvisioning(ProcessingUnit pu) {
            this.pu = pu;
        }
        
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
                    
                    Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
                    
                    Set<GridServiceAgent> usedAgents = getAllUsedGridServiceAgents();
                    while (!MachinesSlaUtils.isCapacityRequirementsMet(agents,capacityRequirements)) {
                        
                        GridServiceAgent agent = tryAllocateFreeGridServiceAgent(usedAgents);
                        if (agent == null) {
                            break;
                        }
                        agents.add(agent);
                        usedAgents.add(agent);
                    }
                    
                    if (exception == null && agents.isEmpty()) {
                        
                        exception = new ExecutionException(
                                new AdminException(
                                        "Out of Machines. "+
                                        "Please start another grid service agent "+
                                        "on a new machine."));
                    }
                    
                    allocatedAgents = agents.toArray(new GridServiceAgent[agents.size()]);
                }

                public Date getTimestamp() {
                    return timestamp;
                }

                public CapacityRequirements getCapacityRequirements() {
                    return capacityRequirements;
                }
            };
        }

        protected GridServiceAgent tryAllocateFreeGridServiceAgent(Set<GridServiceAgent> usedAgents) {
            for (GridServiceAgent agent : admin.getGridServiceAgents()) {
                if (!usedAgents.contains(agent) &&
                    !MachinesSlaUtils.isManagementRunningOnGridServiceAgent(agent)) {
                    return agent;
                }
            }
            return null;
        }

        public void stopMachineAsync(GridServiceAgent agent, long duration, TimeUnit unit) {
            // special case where we do not actually kill the agent.
            logger.info(
                    "Agent machine " + 
                    agent.getMachine().getHostAddress() + " " +
                    "is not actually killed, instead it is moved back to the free machines pool.");
            getAgentsPendingShutdown().remove(agent);
        }
        
        private List<GridServiceAgent> getAgentsPendingShutdown() {
            return agentsPendingShutdownPerProcessingUnit.get(pu);
        }
    }

}
