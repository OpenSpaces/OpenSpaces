package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint {

    private static final Log logger = LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class);

	private static final int START_AGENT_TIMEOUT_FAILURE_SECONDS = 10*60;
    
    private final String zone;
    private final InternalAdmin admin;
    private final NonBlockingElasticScaleHandler machinePool;
    
    private List<FutureGridServiceAgents> futureAgents;
    private List<GridServiceAgent> agentsPendingShutdown;
    
    private boolean destroyed;
    
    public DefaultMachinesSlaEnforcementEndpoint(Admin admin, String zone, NonBlockingElasticScaleHandler machinePool) {
        
    	if (zone == null) {
        	throw new IllegalArgumentException("zone cannot be null.");
        }
        
        if (admin == null) {
        	throw new IllegalArgumentException("admin cannot be null.");
        }
        
        this.zone = zone;
        this.admin = (InternalAdmin) admin;
        this.machinePool = machinePool;
        this.destroyed = false;
        
        this.futureAgents = new ArrayList<FutureGridServiceAgents>();
        this.agentsPendingShutdown = new ArrayList<GridServiceAgent>();
        
    }
    
    public GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        validateNotDestroyed();
       
        List<GridServiceAgent> agents =  Arrays.asList(getAllGridServiceAgents());
        for (GridServiceAgent agent : agentsPendingShutdown) {
            agents.remove(agent);
        }
        return agents.toArray(new GridServiceAgent[]{});
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
        
        
        try {
			run(sla);
		} catch (ConflictingOperationInProgressException e) {
			logger.info("Cannot enforce Machines SLA since a conflicting operation is in progress. Try again later.", e);
            return false; // try again next time
		}
        
        return isSlaReached(sla);
    }


    
    public String getId() {
        return zone;
    }

    
    public void destroy() {
        
        destroyed = true;
    }
    
	private void run(MachinesSlaPolicy sla)
			throws ConflictingOperationInProgressException {

		cleanAgentsMarkedForShutdown();
		cleanFutureAgents();

		int targetMemory = sla.getMemoryCapacityInMB();
		double targetCpu = sla.getCpu();

		int existingMemory = 0;
		double existingCpu = 0;
		for (GridServiceAgent agent : getAllGridServiceAgents()) {
			existingMemory += getMemoryInMB(agent);
			existingCpu += getCpu(agent);
		}

		if (existingMemory > targetMemory && existingCpu > targetCpu) {

			// scale in
			int surplusMemory = existingMemory - targetMemory;
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
				}
			}

			// mark agents for shutdown if there are not enough of them
			for (GridServiceAgent agent : getGridServiceAgents()) {
				int machineMemory = getMemoryInMB(agent);
				double machineCpu = getCpu(agent);
				if (surplusMemory >= machineMemory && surplusCpu >= machineCpu) {

					// mark machine for shutdown unless it is a management
					// machine
					this.agentsPendingShutdown.add(agent);
					surplusMemory -= machineMemory;
					surplusCpu -= machineCpu;
				}
			}
		}

		else if (existingMemory < targetMemory || existingCpu < targetCpu) {
			// scale out

			// unmark all machines pending shutdown
			this.agentsPendingShutdown.clear();

			int shortageMemory = targetMemory - existingMemory;
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

			if (machinePool != null) {
				
				this.futureAgents.add(machinePool.startMachinesAsync(
						zone,
						new CapacityRequirements(
								new MemoryCapacityRequirment(shortageMemory),
								new CpuCapacityRequirement(shortageCpu)),
						START_AGENT_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS));
			
			}
		}
	}

	private void cleanAgentsMarkedForShutdown() {
    	
        final Iterator<GridServiceAgent> iterator = agentsPendingShutdown.iterator();
        while (iterator.hasNext()) {
            final GridServiceAgent agent = iterator.next();
            if (!agent.isRunning()) {
                iterator.remove();
            }
            
            else if (agent.getProcessesDetails().getProcessDetails().length == 0) {
            	// all containers have been closed, and no GSM/ESM/LUS on this agent
        		admin.scheduleAdminOperation(new Runnable() {
                    
                    public void run() {
                        agent.shutdown();
                    }
                });
            }
        }
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
                    if (logger.isInfoEnabled()) {
		            	for (GridServiceAgent agent : agents) {
	                    	logger.info("Agent started succesfully on machine " + agent.getMachine().getHostAddress());
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
    
    /**
     * @return true if reached exact target number of machines with specified parameters.
     */
    private boolean isSlaReached(MachinesSlaPolicy sla) {
    	
    	int totalPhysicalMemorySizeInMB = 0;
    	for (GridServiceAgent agent : this.getGridServiceAgents()) {
    		totalPhysicalMemorySizeInMB += getMemoryInMB(agent);
    	}
    	
    	return sla.getMemoryCapacityInMB() > totalPhysicalMemorySizeInMB;
    }
    
    private GridServiceAgent[] getAllGridServiceAgents() {
        return admin.getZones().getByName(zone).getGridServiceAgents().getAgents();
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
