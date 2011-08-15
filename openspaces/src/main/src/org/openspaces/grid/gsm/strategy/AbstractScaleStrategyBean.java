package org.openspaces.grid.gsm.strategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.ElasticMachineProvisioningAware;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.isolation.DedicatedMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.SharedMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;


public abstract class AbstractScaleStrategyBean implements 
    GridServiceAgentAddedEventListener, 
    GridServiceAgentRemovedEventListener , 
    ElasticMachineProvisioningAware ,
    ProcessingUnitAware,
    ScaleStrategyBean,
    Bean,
    Runnable{
    

    private static final long GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS = 60;
    private static final long GET_DISCOVERED_MACHINES_RETRY_SECONDS = 60;
    
    private static final int MAX_NUMBER_OF_MACHINES = 1000; // a very large number representing max number of machines per pu, but that would not overflow when multiplied by container capacity in MB

    
    // injected 
    private InternalAdmin admin;
    private ProcessingUnit pu;
    private ProcessingUnitSchemaConfig schemaConfig;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private StringProperties properties;
    private ElasticMachineIsolationConfig isolationConfig;
    
    // created by afterPropertiesSet()
    private Log logger;
    private ScheduledFuture<?> scheduledTask;
    private int minimumNumberOfMachines;    
    private ElasticProcessingUnitMachineIsolation isolation;
    
    // state
    private boolean syncAgents;
    private FutureGridServiceAgents futureAgents;
    
    protected InternalAdmin getAdmin() {
        return this.admin;
    }
    
    protected Log getLogger() {
        return this.logger;
    }
    
    protected int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }
    
    public Map<String, String> getProperties() {
        return properties.getProperties();
    }

    public void setProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }


    protected ProcessingUnit getProcessingUnit() {
        return pu;
    }

    public void setElasticMachineIsolation(ElasticMachineIsolationConfig isolationConfig) {
        this.isolationConfig = isolationConfig;
    }
    
    public ElasticProcessingUnitMachineIsolation getIsolation() {
        return isolation;
    }
    
    public void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
    

    protected ProcessingUnitSchemaConfig getSchemaConfig() {
        return schemaConfig;
    }
    
    public void setAdmin(Admin admin) {
        this.admin = (InternalAdmin) admin;
    }
  
    public void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning machineProvisioning) {
        this.machineProvisioning = machineProvisioning;
    }
    
    public NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return machineProvisioning;
    }
    
    public void afterPropertiesSet() {
        
        if (pu == null) {
            throw new IllegalStateException("pu cannot be null");
        }
        
        if (properties == null) {
            throw new IllegalStateException("properties cannot be null.");
        }
        
        if (admin == null) {
            throw new IllegalStateException("admin cannot be null.");
        }
        
        if (machineProvisioning == null) {
            throw new IllegalStateException("machine provisioning cannot be null.");
        }
        
        if (schemaConfig == null) {
            throw new IllegalStateException("schemaConfig cannot be null.");
        }
    
        if (isolationConfig == null) {
            throw new IllegalStateException("isolationConfig cannot be null");
        }
        
        if (isolationConfig.isDedicatedIsolation()) {
            isolation = new DedicatedMachineIsolation(pu.getName());
        }
        else if (isolationConfig.isSharedIsolation()) {
            isolation = new SharedMachineIsolation(isolationConfig.getSharingId());
        }
        else {
            throw new IllegalStateException("unsupported PU isolation");
        }
        
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(this.getClass())),
                    pu);
        
        logger.info("properties: "+properties);
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(this);

        minimumNumberOfMachines = calcMinimumNumberOfMachines();
        int pollingIntervalSeconds = ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
        syncAgents = true;
        scheduledTask = 
        (admin).scheduleWithFixedDelayNonBlockingStateChange(
                this, 
                0L, 
                pollingIntervalSeconds, 
                TimeUnit.SECONDS);
        
        
        logger.debug(pu.getName() + " is being monitored for SLA violations every " + pollingIntervalSeconds + " seconds");
    }

    public void destroy() {
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().remove(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().remove(this);
        
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    protected Collection<GridServiceAgent> getDiscoveredAgents() throws AgentsNotYetDiscoveredException {
        
        if (futureAgents == null || !futureAgents.isDone()) {
            throw new AgentsNotYetDiscoveredException(
                    "Need to wait until retrieved list of machines from " + machineProvisioning.getClass() );
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
            throw new AgentsNotYetDiscoveredException("Failed retrieving list of machines from " + machineProvisioning.getClass() , e);
        } catch (TimeoutException e) {
            throw new AgentsNotYetDiscoveredException("Failed retrieving list of machines from " + machineProvisioning.getClass(), e);
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
        
        logger.debug("Enforcing sla for processing unit " + pu.getName());
        //TODO: Move this check to EsmImpl, this component should not be aware it is running in an ESM
        //TODO: Raise an alert
        int numberOfEsms = admin.getElasticServiceManagers().getSize();
        if (numberOfEsms != 1) {
            logger.error("Number of ESMs must be 1. Currently " + numberOfEsms + " running.");
            return;
        }
    
        if (syncAgents) {
            syncAgents = false;
            futureAgents = machineProvisioning.getDiscoveredMachinesAsync(GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        
        if (futureAgents != null &&  futureAgents.getException() != null) {
            
            logger.error("Failed retrieving list of machines from " + machineProvisioning.getClass() + ". " +
                         "Retrying in " + GET_DISCOVERED_MACHINES_RETRY_SECONDS + " seconds.", futureAgents.getException());
            
            admin.scheduleOneTimeWithDelayNonBlockingStateChange(new Runnable() {
    
                public void run() {
                   syncAgents = true;
                }},
                GET_DISCOVERED_MACHINES_RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }
    
    
    private int calcMinimumNumberOfMachines() {
        
        if (getSchemaConfig().isDefaultSchema()) {
            return 1;
        }
        
        if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
            int minNumberOfMachines;
            if (getProcessingUnit().getMaxInstancesPerMachine() == 0) {
                minNumberOfMachines = 1;
                getLogger().info("minNumberOfMachines=1 (since max instances from same partition per machine is not defined)");
            }
            
            else {
                minNumberOfMachines = (int)Math.ceil(
                        (1 + getProcessingUnit().getNumberOfBackups())/(1.0*getProcessingUnit().getMaxInstancesPerMachine()));
                getLogger().info("minNumberOfMachines= " +
                        "ceil((1+backupsPerPartition)/maxInstancesPerPartitionPerMachine)= "+
                        "ceil("+(1+getProcessingUnit().getNumberOfBackups())+"/"+getProcessingUnit().getMaxInstancesPerMachine() + ")= " +
                        minNumberOfMachines);
            }
            
            return minNumberOfMachines;
        }
        
        throw new BeanConfigurationException(
                "Processing Unit " + pu.getName() + 
                "needs to be either stateless, or stateful or a space (it is " + schemaConfig.getSchema());
        
    }
    
    protected int getMaximumNumberOfInstances() {
        if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
            return getProcessingUnit().getTotalNumberOfInstances();
        }
        else {
            return MAX_NUMBER_OF_MACHINES;
        }
    }

    public Fraction getContainerNumberOfCpuCores(ManualCapacityScaleConfig slaConfig) {
        if (getSchemaConfig().isPartitionedSync2BackupSchema()) {
            Fraction cpuCores = MachinesSlaUtils.convertCpuCoresFromDoubleToFraction(slaConfig.getNumberOfCpuCores());
            return cpuCores.divide(pu.getNumberOfInstances());
        }
        else {
            return Fraction.ONE;
        }
    }
    
    static class AgentsNotYetDiscoveredException extends Exception {
        private static final long serialVersionUID = 1L; 
        public AgentsNotYetDiscoveredException(String message, Exception inner) {
            super(message,inner);
        }
        public AgentsNotYetDiscoveredException(String message) {
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
}
