package org.openspaces.grid.gsm.strategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.FailedToDiscoverMachinesException;
import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioningException;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;


public class ElasticMachineProvisioningDiscoveredMachinesCache implements 
    DiscoveredMachinesCache,
    GridServiceAgentAddedEventListener, 
    GridServiceAgentRemovedEventListener , 
    Runnable{
    

    private static final long GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS = 60;
    private static final long GET_DISCOVERED_MACHINES_RETRY_SECONDS = 60;
    
    
    // injected 
    private ProcessingUnit pu;
    private InternalAdmin admin;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    
    // created by afterPropertiesSet()
    private Log logger;
    private ScheduledFuture<?> scheduledTask;
    
    // state
    private boolean syncAgents;
    private FutureGridServiceAgents futureAgents;
    

    public ElasticMachineProvisioningDiscoveredMachinesCache(ProcessingUnit pu, NonBlockingElasticMachineProvisioning machineProvisioning,long pollingIntervalSeconds) {
        
        this.pu = pu;
        
        this.admin = (InternalAdmin) pu.getAdmin();
        
        this.machineProvisioning = machineProvisioning;
       
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(this.getClass())),
                    pu);
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(this);

        syncAgents = true;
        
        scheduledTask = 
            admin.scheduleWithFixedDelayNonBlockingStateChange(
                    this, 
                    0L, 
                    pollingIntervalSeconds, 
                    TimeUnit.SECONDS);
                
    }

    public void destroy() {
    
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().remove(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().remove(this);
       
    }
    
    public Collection<GridServiceAgent> getDiscoveredAgents() throws WaitingForDiscoveredMachinesException, FailedToDiscoverMachinesException {
        
        if (futureAgents == null || !futureAgents.isDone()) {
            throw new WaitingForDiscoveredMachinesException(
                    "Need to wait until retrieved list of machines.");
        }
        
        Set<GridServiceAgent> filteredAgents = new HashSet<GridServiceAgent>(); 
        
        try {
            GridServiceAgent[] agents = futureAgents.get();
            for (GridServiceAgent agent : agents) {
                if (!agent.isDiscovered()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Agent " + MachinesSlaUtils.machineToString(agent.getMachine()) + " has shutdown.");
                    }
                }
                else if (!MachinesSlaUtils.isAgentConformsToMachineProvisioningConfig(agent, machineProvisioning.getConfig())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Agent " + MachinesSlaUtils.machineToString(agent.getMachine()) + " does not conform to machine provisioning SLA.");
                    }
                }
                else {
                    filteredAgents.add(agent);
                }
            }
            //TODO: Move this sort into the bin packing solver. It already has the priority of each machine
            // so it can sort it by itself.
            List<GridServiceAgent> sortedFilteredAgents = MachinesSlaUtils.sortManagementFirst(filteredAgents);
            if (logger.isDebugEnabled()) {
                logger.debug("Provisioned Agents: " + MachinesSlaUtils.machinesToString(sortedFilteredAgents));
            }
            return sortedFilteredAgents;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            
            if (cause instanceof ElasticMachineProvisioningException) {
                throw new FailedToDiscoverMachinesException(pu, e);
            }
            
            if (cause instanceof AdminException) {
                throw new FailedToDiscoverMachinesException(pu, e);
            }
            throw new IllegalStateException("Unexpected exception type",e);
            
        } catch (TimeoutException e) {
            throw new FailedToDiscoverMachinesException(pu, e);
        }
    }
    
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        syncAgents = true;
    }

    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        syncAgents = true;
    }

    /**
     * Synchronizes the list of agents with the machine provisioning bean
     * We use the syncAgents flag to make sure there is no more than one concurrent call to machineProvisioning
     */
    public void run() {
        
        
        //TODO: Move this check to EsmImpl, this component should not be aware it is running in an ESM
        //TODO: Raise an alert
        int numberOfEsms = admin.getElasticServiceManagers().getSize();
        if (numberOfEsms != 1) {
            logger.error("Number of ESMs must be 1. Currently " + numberOfEsms + " running.");
            return;
        }
    
        if (syncAgents) {
            syncAgents = false;
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieving list of provisioned machines");
            }
            futureAgents = machineProvisioning.getDiscoveredMachinesAsync(GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        
        if (futureAgents != null &&  futureAgents.getException() != null) {
            
            if (logger.isDebugEnabled()) {
                logger.debug("Failed retrieving list of machines. " +
                             "Retrying in " + GET_DISCOVERED_MACHINES_RETRY_SECONDS + " seconds.", futureAgents.getException());
            }
            
            admin.scheduleOneTimeWithDelayNonBlockingStateChange(new Runnable() {
    
                public void run() {
                   syncAgents = true;
                }},
                GET_DISCOVERED_MACHINES_RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }
}