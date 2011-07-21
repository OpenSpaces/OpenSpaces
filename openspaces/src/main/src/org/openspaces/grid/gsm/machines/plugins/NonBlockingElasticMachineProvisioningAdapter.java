package org.openspaces.grid.gsm.machines.plugins;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgent;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;

/**
 * An adapter that wraps an {@link ElasticMachineProvisioning} and exposes a {@link NonBlockingElasticMachineProvisioning}
 *
 * @see NonBlockingElasticMachineProvisioning
 * @see ElasticMachineProvisioning
 * 
 * @author itaif
 */
public class NonBlockingElasticMachineProvisioningAdapter implements NonBlockingElasticMachineProvisioning {

	private ElasticMachineProvisioning machineProvisioning;

    private final ExecutorService executorService;

    private final ScheduledThreadPoolExecutor scheduledExecutorService;

	private static final Log logger = LogFactory
			.getLog(NonBlockingElasticMachineProvisioningAdapter.class);

    private static final int THROTTLING_DELAY_SECONDS = 10;
    
	public NonBlockingElasticMachineProvisioningAdapter(ElasticMachineProvisioning machineProvisioning, ExecutorService executorService, ScheduledThreadPoolExecutor scheduledExecutorService) {
		this.machineProvisioning = machineProvisioning;
		this.executorService = executorService;
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public FutureGridServiceAgent[] startMachinesAsync(
			final CapacityRequirements capacityRequirements,
			final long duration, final TimeUnit unit) {
		
	    if (!isStartMachineSupported()) {
            throw new UnsupportedOperationException();
        }
	    
	    final CapacityRequirements singleMachineCapacity = machineProvisioning.getCapacityOfSingleMachine();
	    int numberOfMachines = calcNumberOfMachines(capacityRequirements, machineProvisioning);
	    FutureGridServiceAgent[] futureAgents = new FutureGridServiceAgent[numberOfMachines];
	    
	    for (int i = 0 ; i < futureAgents.length ; i++) {
    		final AtomicReference<Object> ref = new AtomicReference<Object>(null);
    		
    		final int throttlingDelay = i*THROTTLING_DELAY_SECONDS;
    		final long start = System.currentTimeMillis();
    		final long end = start+ throttlingDelay+unit.toMillis(duration);
    		submit(new Runnable() {
    			public void run() {
    				try {
    					GridServiceAgent agent = machineProvisioning.startMachine(duration, unit);
    					ref.set(agent);
    				} catch (ElasticMachineProvisioningException e) {
    					ref.set(new ExecutionException(e));
    				} catch (InterruptedException e) {
    					ref.set(new ExecutionException(e));
    				} catch (Exception e) {
    					logger.error("Unexpected exception", e);
    					ref.set(new ExecutionException(e));
    				}
    			}
    
    		},throttlingDelay,TimeUnit.SECONDS);

    		futureAgents[i] = new FutureGridServiceAgent() {
    
    			public boolean isDone() {
    				return  System.currentTimeMillis() > end || ref.get() != null;
    			}
    			
    			public ExecutionException getException() {
    				Object result = ref.get();
    				if (result != null && result instanceof ExecutionException) {
    					return (ExecutionException) result;
    				}
    				return null;
    			}
    
    			public boolean isTimedOut() {
    				Object result = ref.get();
    				return System.currentTimeMillis() > end || 
    					   (result != null && 
    					    result instanceof ExecutionException && 
    					    ((ExecutionException)result).getCause() != null && 
    					    ((ExecutionException)result).getCause() instanceof TimeoutException);
    			}
    
    
    			public Date getTimestamp() {
    				return new Date(start);
    			}
    
    			public GridServiceAgent get()
    					throws ExecutionException, IllegalStateException,TimeoutException {
    
    			    Object result = ref.get();
    			    
    			    if (result == null) {
        				if (System.currentTimeMillis() > end) {
        					throw new TimeoutException(
        							"Starting a new machine took more than "
        									+ unit.toSeconds(duration)
        									+ " seconds to complete.");
        				}
    				
    					throw new IllegalStateException(
    							"Async operation is not done yet.");
    				}
    
    				if (result instanceof Exception) {
    					throw new ExecutionException((Exception) result);
    				}
    
    				GridServiceAgent agent = (GridServiceAgent) result;
    
    				return agent;
    			}
    			
                public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
                    return NonBlockingElasticMachineProvisioningAdapter.this;
                }

                public CapacityRequirements getFutureCapacity() {
                    return singleMachineCapacity;
                }
    		};
	    }
		return futureAgents;
	}

	public void stopMachineAsync(final GridServiceAgent agent, final long duration,
			final TimeUnit unit) {

	    if (!isStartMachineSupported()) {
	        throw new UnsupportedOperationException();
	    }
	    
		final String hostAddress = agent.getMachine().getHostAddress();
		
		submit(new Runnable() {
			public void run() {
				try {
					if (NonBlockingElasticMachineProvisioningAdapter.this.machineProvisioning.stopMachine(agent, duration, unit)) {
					    logger.info(hostAddress + " stopped succesfully.");
					}
				} catch (ElasticMachineProvisioningException e) {
					logger.warn("Error while stopping " + hostAddress,e);
				} catch (InterruptedException e) {
					logger.info("Interrupted while stopping " + hostAddress,e);
				} catch (TimeoutException e) {
					logger.info("Stopping " + hostAddress + " times out.",e);
				}
			}
		});
	}
	
	private void submit(Runnable runnable) {
        executorService.submit(runnable);
    }
    
	private void submit(final Runnable runnable, long delay, TimeUnit unit) {
	    scheduledExecutorService.schedule(new Runnable() {
	        public void run() {
                submit(runnable);
            }}
	    ,delay,unit);
    }
	
    public FutureGridServiceAgents getDiscoveredMachinesAsync(final long duration, final TimeUnit unit) {

        final AtomicReference<Object> ref = new AtomicReference<Object>(null);
        final long start = System.currentTimeMillis();
        final long end = start + unit.toMillis(duration);

        submit(new Runnable() {
            public void run() {
                try {
                    GridServiceAgent[] agents = machineProvisioning.getDiscoveredMachines(duration, unit);
                    ref.set(agents);
                } catch (ElasticMachineProvisioningException e) {
                    ref.set(new ExecutionException(e));
                } catch (InterruptedException e) {
                    ref.set(new ExecutionException(e));
                } catch (Exception e) {
                    logger.error("Unexpected exception", e);
                    ref.set(new ExecutionException(e));
                }
            }

        });

        return new FutureGridServiceAgents() {

            public boolean isDone() {
                return  System.currentTimeMillis() > end || ref.get() != null;
            }
            
            public ExecutionException getException() {
                Object result = ref.get();
                if (result != null && result instanceof ExecutionException) {
                    return (ExecutionException) result;
                }
                return null;
            }

            public boolean isTimedOut() {
                Object result = ref.get();
                return System.currentTimeMillis() > end || 
                       (result != null && 
                        result instanceof ExecutionException && 
                        ((ExecutionException)result).getCause() != null && 
                        ((ExecutionException)result).getCause() instanceof TimeoutException);
            }


            public Date getTimestamp() {
                return new Date(start);
            }

            public GridServiceAgent[] get()
                    throws ExecutionException, IllegalStateException,TimeoutException {

                Object result = ref.get();
                
                if (result == null) {
                    if (System.currentTimeMillis() > end) {
                        throw new TimeoutException(
                                "Starting a new machine took more than "
                                        + unit.toSeconds(duration)
                                        + " seconds to complete.");
                    }
                
                    throw new IllegalStateException(
                            "Async operation is not done yet.");
                }

                if (result instanceof Exception) {
                    throw new ExecutionException((Exception) result);
                }

                return (GridServiceAgent[]) result;
            }
        };
    }

    public ElasticMachineProvisioningConfig getConfig() {
        return machineProvisioning.getConfig();
    }

    public boolean isStartMachineSupported() {
        return machineProvisioning.isStartMachineSupported();
    }
    
    private static int calcNumberOfMachines(
            CapacityRequirements capacityRequirements, 
            ElasticMachineProvisioning machineProvisioning) {
        
        NumberOfMachinesCapacityRequirement numberOfMachinesCapacityRequirement = capacityRequirements.getRequirement(new NumberOfMachinesCapacityRequirement().getType());
        
        int maxNumberOfMachines = 
            Math.max (1,numberOfMachinesCapacityRequirement.getNumberOfMachines());
        
        CapacityRequirements singleMachineCapacityRequirements = 
            machineProvisioning.getCapacityOfSingleMachine()
            .subtractOrZero(machineProvisioning.getConfig().getReservedCapacityPerMachine());
        
        for (CapacityRequirement capacityRequirement : capacityRequirements.getRequirements()) {
            CapacityRequirement singleMachinecapacityRequirement = 
                singleMachineCapacityRequirements.getRequirement(capacityRequirement.getType());
            if (!singleMachinecapacityRequirement.equalsZero()) {
                int numberOfMachines = (int) Math.ceil(capacityRequirement.divide(singleMachinecapacityRequirement));
                if (numberOfMachines > maxNumberOfMachines) {
                    maxNumberOfMachines =  numberOfMachines;
                }
            }
        }
        
        logger.info(maxNumberOfMachines + " "+
                "machines are required in order to satisfy capacity requirements: " + capacityRequirements);
        
        return maxNumberOfMachines;
    }
}
