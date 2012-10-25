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
import org.openspaces.admin.gsa.GSAReservationId;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgent;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.exceptions.NoClassDefFoundElasticMachineProvisioningException;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticGridServiceAgentProvisioningException;
import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticMachineProvisioningException;

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

    private static final int THROTTLING_DELAY_SECONDS = 0;
    
	public NonBlockingElasticMachineProvisioningAdapter(ElasticMachineProvisioning machineProvisioning, ExecutorService executorService, ScheduledThreadPoolExecutor scheduledExecutorService) {
	    this.machineProvisioning = machineProvisioning;
		this.executorService = executorService;
		this.scheduledExecutorService = scheduledExecutorService;
	}
	
	@Override
    public FutureGridServiceAgent[] startMachinesAsync(
            final CapacityRequirements capacityRequirements,
            final ExactZonesConfig zones, 
            final long duration, final TimeUnit unit) {

        if (!isStartMachineSupported()) {
            throw new UnsupportedOperationException();
        }

        final GSAReservationId reservationId = GSAReservationId.randomGSAReservationId();
        final CapacityRequirements singleMachineCapacity = machineProvisioning.getCapacityOfSingleMachine();
        int numberOfMachines = calcNumberOfMachines(capacityRequirements, machineProvisioning);
        FutureGridServiceAgent[] futureAgents = new FutureGridServiceAgent[numberOfMachines];
        
        for (int i = 0 ; i < futureAgents.length ; i++) {
            final AtomicReference<Object> ref = new AtomicReference<Object>(null);
            
            final int throttlingDelay = i*THROTTLING_DELAY_SECONDS;
            final long start = System.currentTimeMillis();
            final long end = start+ throttlingDelay*1000+unit.toMillis(duration);
            submit(new Runnable() {
                public void run() {
                    try {
                        logger.info("Starting a new machine");
                        
                        GridServiceAgent agent = machineProvisioning.startMachine(zones, reservationId, duration, unit);
                        ref.set(agent);
                        logger.info("New machine started");
                    } catch (ElasticMachineProvisioningException e) {
                        ref.set(e);
                    } catch (ElasticGridServiceAgentProvisioningException e) {
                        ref.set(e);
                    } catch (InterruptedException e) {
                        ref.set(e);
                    } catch (TimeoutException e) {
                        ref.set(e);
                    } catch (NoClassDefFoundError e) {
                        ref.set((new NoClassDefFoundElasticMachineProvisioningException(e)));
                    } catch (Throwable e) {
                        logger.error("Unexpected exception:" + e.getMessage(), e);
                        ref.set(e);
                    }
                }
    
            },throttlingDelay,TimeUnit.SECONDS);

            futureAgents[i] = new FutureGridServiceAgent() {
    
                public boolean isDone() {
                    return  System.currentTimeMillis() > end || ref.get() != null;
                }
                
                public ExecutionException getException() {
                    Object result = ref.get();
                    if (result != null && result instanceof Throwable) {
                        return new ExecutionException((Throwable)result);
                    }
                    return null;
                }
    
                public boolean isTimedOut() {
                    Object result = ref.get();
                    return System.currentTimeMillis() > end || 
                           (result != null && result instanceof TimeoutException);
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
    
                    if (getException() != null) {
                        throw getException();
                    }
    
                    return (GridServiceAgent) result;
                }
                
                public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
                    return NonBlockingElasticMachineProvisioningAdapter.this;
                }

                public CapacityRequirements getFutureCapacity() {
                    return singleMachineCapacity;
                }
                
                public GSAReservationId getReservationId() {
                    return reservationId;
                }
            };
        }
        return futureAgents;

        
    }	
	
    @Override
	public void stopMachineAsync(final GridServiceAgent agent, final long duration,
			final TimeUnit unit) {

	    if (!isStartMachineSupported()) {
	        throw new UnsupportedOperationException();
	    }
	    
		final String hostAddress = agent.getMachine().getHostAddress();
		
		submit(new Runnable() {
			public void run() {
				try {
				    
		            logger.info("Stopping machine " + hostAddress);
			    	if (NonBlockingElasticMachineProvisioningAdapter.this.machineProvisioning.stopMachine(agent, duration, unit)) {
					    logger.info("machine " + hostAddress + " succesfully stopped.");
					}
				} catch (ElasticMachineProvisioningException e) {
					logger.warn("Error while stopping " + hostAddress,e);
				} catch (ElasticGridServiceAgentProvisioningException e) {
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
	
	@Override
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
                    ref.set(e);
                } catch (ElasticGridServiceAgentProvisioningException e) {
                    ref.set(e);
                } catch (InterruptedException e) {
                    ref.set(e);
                } catch (TimeoutException e) {
                    ref.set(e);
                } catch (NoClassDefFoundError e) {
                    ref.set((new NoClassDefFoundElasticMachineProvisioningException(e)));
                } catch (Throwable e) {
                    logger.error("Unexpected exception", e);
                    ref.set(e);
                }
            }

        });

        return new FutureGridServiceAgents() {

            public boolean isDone() {
                return  System.currentTimeMillis() > end || ref.get() != null;
            }
            
            public ExecutionException getException() {
                Object result = ref.get();
                if (result != null && result instanceof Throwable) {
                    return new ExecutionException((Throwable)result);
                }
                return null;
            }

            public boolean isTimedOut() {
                Object result = ref.get();
                return System.currentTimeMillis() > end || 
                       (result != null && 
                        result instanceof TimeoutException);
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

                if (getException() != null) {
                    throw getException();
                }
                
                return (GridServiceAgent[]) result;
                
            }
        };
    }

	@Override
    public ElasticMachineProvisioningConfig getConfig() {
        return machineProvisioning.getConfig();
    }

	@Override
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

    @Override
    public void setElasticProcessingUnitMachineIsolation(ElasticProcessingUnitMachineIsolation isolation) {
        machineProvisioning.setElasticProcessingUnitMachineIsolation(isolation);
    }

    @Override
    public void setElasticMachineProvisioningProgressChangedEventListener(
            ElasticMachineProvisioningProgressChangedEventListener machineEventListener) {
        machineProvisioning.setElasticMachineProvisioningProgressChangedEventListener(machineEventListener);
    }

    @Override
    public void setElasticGridServiceAgentProvisioningProgressEventListener(
            ElasticGridServiceAgentProvisioningProgressChangedEventListener agentEventListener) {
        machineProvisioning.setElasticGridServiceAgentProvisioningProgressEventListener(agentEventListener);
    }
    
}
