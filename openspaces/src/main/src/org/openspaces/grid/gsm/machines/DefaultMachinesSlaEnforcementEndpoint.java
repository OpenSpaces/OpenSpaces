package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint {

    private static final Log logger = LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class);

	private static final int START_AGENT_TIMEOUT_SECONDS = 10*60;
    private static final long STOP_AGENT_TIMEOUT_SECONDS = 10*60;
    
    private final ProcessingUnit pu;
    private final InternalAdmin admin;
        
    private List<GridServiceAgent> agentsStarted;
    private List<FutureGridServiceAgents> futureAgents;
    private List<GridServiceAgent> agentsPendingShutdown;
    
    private boolean destroyed;
    
    public DefaultMachinesSlaEnforcementEndpoint(Admin admin, ProcessingUnit pu) {
        
        if (admin == null) {
        	throw new IllegalArgumentException("admin cannot be null.");
        }
        
        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null.");
        }
        
        this.admin = (InternalAdmin) admin;
        this.pu = pu;
        this.destroyed = false;
        
        this.futureAgents = new ArrayList<FutureGridServiceAgents>();
        this.agentsPendingShutdown = new ArrayList<GridServiceAgent>();
        
    }
    
    public GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDestroyed();
       
        return agentsStarted.toArray(new GridServiceAgent[]{});
    }

    private List<GridServiceAgent> getGridServiceAgentsSortManagementComponentsLast() {
        List<GridServiceAgent> agents = Arrays.asList(getGridServiceAgents());
        Collections.sort(agents,new Comparator<GridServiceAgent>() {

            public int compare(GridServiceAgent agent1, GridServiceAgent agent2) {
                int numberOfManagementComponents1 = getNumberOfChildProcesses(agent1) - getNumberOfChildContainers(agent1);
                int numberOfManagementComponents2 = getNumberOfChildProcesses(agent2) - getNumberOfChildContainers(agent2);
                return numberOfManagementComponents1 - numberOfManagementComponents2;
            }
        });
        return agents;
    }
    
    public GridServiceAgent[] getGridServiceAgentsPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDestroyed();
        
        return agentsPendingShutdown.toArray(new GridServiceAgent[] {});
        
    }

    public boolean enforceSla(MachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDestroyed();
        
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
            throw new IllegalArgumentException("MachineProvisioning cannot be null.");
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

    
    public void destroy() {
        
        destroyed = true;
    }
    
	private boolean enforceSlaInternal(MachinesSlaPolicy sla)
			throws ConflictingOperationInProgressException {

		cleanAgentsMarkedForShutdown(sla.getMachineProvisioning());
		cleanFutureAgents();

		boolean slaReached = futureAgents.size() == 0 && agentsPendingShutdown.size() == 0;
		
		long targetMemory = sla.getMemoryCapacityInMB();
		double targetCpu = sla.getCpu();

		int existingMemory = 0;
		double existingCpu = 0;
		
		for (GridServiceAgent agent : agentsStarted) {
			existingMemory += getMemoryInMB(agent);
			existingCpu += getCpu(agent);
		}
		
		for (GridServiceAgent agent : agentsPendingShutdown) {
            existingMemory += getMemoryInMB(agent);
            existingCpu += getCpu(agent);
        }

		if (existingMemory > targetMemory && existingCpu > targetCpu) {

			// scale in
			long surplusMemory = existingMemory - targetMemory;
			double surplusCpu = existingCpu - targetCpu;

			// adjust existingMemory based on agents marked for shutdown
			// remove mark if it would cause surplus to be below zero.
			Iterator<GridServiceAgent> iterator = Arrays.asList(
					getGridServiceAgentsPendingShutdown()).iterator();
			while (iterator.hasNext()) {

				GridServiceAgent agent = iterator.next();
				int machineMemory = getMemoryInMB(agent);
				double machineCpu = getCpu(agent);
				if (surplusMemory >= machineMemory && surplusCpu >= machineCpu) {
					// this machine is already marked for shutdown, so surplus
					// is adjusted to reflect that
					surplusMemory -= machineMemory;
					surplusCpu -= machineCpu;
				} else {
					// don't mark this machine for shutdown otherwise surplus
					// would become negative
					iterator.remove();
					logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
				}
			}

			// mark agents for shutdown if there are not enough of them
			// give priority to agents that do not host a GSM/LUS since we want to evacuate those last.
			for (GridServiceAgent agent : getGridServiceAgentsSortManagementComponentsLast()) {
				int machineMemory = getMemoryInMB(agent);
				double machineCpu = getCpu(agent);
				if (surplusMemory >= machineMemory && surplusCpu >= machineCpu) {

					// mark machine for shutdown unless it is a management
					// machine
					this.agentsPendingShutdown.add(agent);
					surplusMemory -= machineMemory;
					surplusCpu -= machineCpu;
					slaReached = false;
					logger.info("machine agent " + agent.getMachine().getHostAddress() + " is marked for shutdown in order to reduce capacity.");
				}
			}
		}

		else if (existingMemory < targetMemory || existingCpu < targetCpu) {
			// scale out

			// unmark all machines pending shutdown
			for (GridServiceAgent agent : agentsPendingShutdown) {
			    logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
			}
			this.agentsPendingShutdown.clear();
			    

			long shortageMemory = targetMemory - existingMemory;
			double shortageCpu = targetCpu - existingCpu;

			// take into account expected machines into shortage calculate
			for (FutureGridServiceAgents future : this.futureAgents) {
				
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
			
				
				this.futureAgents.add(
			        sla.getMachineProvisioning().startMachinesAsync(
						new CapacityRequirements(
								new MemoryCapacityRequirment(shortageMemory),
								new CpuCapacityRequirement(shortageCpu)),
						START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
				logger.info("One or more new machine were scheduled to be started in order to increase capacity.");
			}
		}
		
		return slaReached;
	}

    private void cleanAgentsMarkedForShutdown(NonBlockingElasticMachineProvisioning machineProvisioning) {
    	
        final Iterator<GridServiceAgent> iterator = agentsPendingShutdown.iterator();
        while (iterator.hasNext()) {
            
            final GridServiceAgent agent = iterator.next();
            
            int numberOfChildProcesses = getNumberOfChildProcesses(agent);
            
            if (!agent.isRunning()) {
                logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is confirmed to be shutdown.");
                iterator.remove();
            } 
            else if (numberOfChildProcesses == 0) {
                 // nothing running on this agent (not even GSM/LUS). Get rid of it.
                logger.info("Stopping agent machine " + agent.getMachine().getHostAddress());	
                machineProvisioning.stopMachineAsync(agent, STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
        
    }

    private int getNumberOfChildProcesses(final GridServiceAgent agent) {
        int numberOfChildProcesses = agent.getProcessesDetails().getProcessDetails().length;
        return numberOfChildProcesses;
    }

    private int getNumberOfChildContainers(final GridServiceAgent agent) {
        int numberOfContainers = 0;
        for (final GridServiceContainer container : admin.getGridServiceContainers()) {
            if (container.getGridServiceAgent() != null && container.getGridServiceAgent().equals(agent)) {
                numberOfContainers++;
            }
        }
        return numberOfContainers;
    }

    private void cleanFutureAgents() {
	    final Iterator<FutureGridServiceAgents> iterator = futureAgents.iterator();
	    while (iterator.hasNext()) {
	        FutureGridServiceAgents future = iterator.next();
	        
	        if (future.isDone()) {
	        
	            iterator.remove();
	            
	            Throwable exception = null;
	            
	            try {
	            
	            	GridServiceAgent[] agents = future.get();
                    agentsStarted.addAll(Arrays.asList(agents));
	            	if (logger.isInfoEnabled()) {
		            	for (GridServiceAgent agent : agents) {
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
    
    private void validateNotDestroyed() {
        if (destroyed) {
            throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
        }
    }
    
    private int getMemoryInMB(GridServiceAgent agent) {
    	return (int) 
			agent.getMachine().getOperatingSystem()
			.getDetails()
			.getTotalPhysicalMemorySizeInMB();
    }

    private double getCpu(GridServiceAgent agent) {
		return agent.getMachine().getOperatingSystem().getDetails().getAvailableProcessors();
	}

    @SuppressWarnings("serial")
	class ConflictingOperationInProgressException extends Exception  {}
}
