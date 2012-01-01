package org.openspaces.grid.gsm.strategy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitInstanceProvisioningFailureEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitInstanceProvisioningProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitScaleProgressChangedEvent;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.ElasticMachineProvisioningAware;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.isolation.DedicatedMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.SharedMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public abstract class AbstractScaleStrategyBean implements 
    ElasticMachineProvisioningAware ,
    ProcessingUnitAware,
    ElasticScaleStrategyEventStorageAware,
    ScaleStrategyBean,
    Bean,
    Runnable{
    
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
    private int minimumNumberOfMachines;    
    private ElasticProcessingUnitMachineIsolation isolation;
    private ScheduledFuture<?> scheduledTask;
        
    // state
    private ProvisionedMachinesCache provisionedMachines;
    private boolean isScaleInProgress;

    // events state 
    private boolean puProvisioningInProgressEventRaised;
    private boolean puProvisioningCompletedEventRaised;
    private boolean containerProvisioningInProgressEventRaised;
    private boolean containerProvisioningCompletedEventRaised;
    private boolean machineProvisioningInProgressEventRaised;
    private boolean machineProvisioningCompletedEventRaised;
    private boolean agentProvisioningCompletedEventRaised;
    private boolean agentProvisioningInProgressEventRaised;

    private ElasticScaleStrategyEventStorage eventQueue;
    
    
    protected InternalAdmin getAdmin() {
        return this.admin;
    }
    
    protected Log getLogger() {
        return this.logger;
    }
    
    protected int getMinimumNumberOfMachines() {
        return minimumNumberOfMachines;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public void setProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }


    protected ProcessingUnit getProcessingUnit() {
        return pu;
    }
    
    protected long getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    @Override
    public void setElasticMachineIsolation(ElasticMachineIsolationConfig isolationConfig) {
        this.isolationConfig = isolationConfig;
    }
    
    protected ElasticProcessingUnitMachineIsolation getIsolation() {
        return isolation;
    }

    @Override
    public void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }

    protected ProcessingUnitSchemaConfig getSchemaConfig() {
        return schemaConfig;
    }
    
    @Override
    public void setAdmin(Admin admin) {
        this.admin = (InternalAdmin) admin;
    }
  
    @Override
    public void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning machineProvisioning) {
        this.machineProvisioning = machineProvisioning;
    }
    
    protected NonBlockingElasticMachineProvisioning getMachineProvisioning() {
        return machineProvisioning;
    }
    
    @Override
    public void setElasticScaleStrategyEventStorage(ElasticScaleStrategyEventStorage eventQueue) {
        this.eventQueue = eventQueue;
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
    
        minimumNumberOfMachines = calcMinimumNumberOfMachines();
        provisionedMachines = new ProvisionedMachinesCache(pu,machineProvisioning, getPollingIntervalSeconds());
        
        isScaleInProgress = true;
        
        scheduledTask = 
            admin.scheduleWithFixedDelayNonBlockingStateChange(
                    this, 
                    0L, 
                    getPollingIntervalSeconds(), 
                    TimeUnit.SECONDS);
        
        logger.debug(pu.getName() + " is being monitored for SLA violations every " + getPollingIntervalSeconds() + " seconds");
    }

    public void destroy() {
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        
        provisionedMachines.destroy();
        
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    protected Collection<GridServiceAgent> getDiscoveredAgents() throws org.openspaces.grid.gsm.strategy.ProvisionedMachinesCache.AgentsNotYetDiscoveredException {

        return provisionedMachines.getDiscoveredAgents();
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
    

    @Override
    public void run() {

        boolean isException = true;
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("enforcing SLA.");
            }
            
            enforceSla();
            
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("SLA enforced.");
            }
            
            isException = false;
        }
        catch (SlaEnforcementInProgressException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("SLA has not been reached",e);
            }
        }
        catch (Exception e) {
            getLogger().error("Unhandled Exception",e);
        }
        finally {
            isScaleInProgress = isException;
            
            boolean isCompleted = !isScaleInProgress;
            eventQueue.enqueu(
                    new ElasticProcessingUnitScaleProgressChangedEvent(isCompleted, isUndeploying(), getProcessingUnit().getName()));
        }
    }

    protected abstract void enforceSla() throws SlaEnforcementInProgressException;
    
    public boolean isScaleInProgress() {
        return isScaleInProgress;
    }
    
    protected void agentProvisioningCompletedEvent() {
        if (!agentProvisioningCompletedEventRaised) {
            boolean isComplete = true;
            eventQueue.enqueu(
                    new ElasticGridServiceAgentProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
            agentProvisioningCompletedEventRaised = true;
            agentProvisioningInProgressEventRaised = false;
        }
    }
    
    protected void machineProvisioningCompletedEvent() {
        if (!machineProvisioningCompletedEventRaised) {
            machineProvisioningCompletedEventRaised = true;
            machineProvisioningInProgressEventRaised = false;

            boolean isComplete = true;
            eventQueue.enqueu(
                    new ElasticMachineProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        }
    }
    
    protected void agentProvisioningInProgressEvent(GridServiceAgentSlaEnforcementInProgressException e) {
        if (!agentProvisioningInProgressEventRaised) {
            boolean isComplete = false;
            eventQueue.enqueu(
                    new ElasticGridServiceAgentProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
            agentProvisioningInProgressEventRaised = true;
            agentProvisioningCompletedEventRaised = false;
        }
        
        if (e instanceof SlaEnforcementFailure) {
            eventQueue.enqueu(
                 new ElasticGridServiceAgentProvisioningFailureEvent(e.getMessage(), ((SlaEnforcementFailure)e).getAffectedProcessingUnits()));
        }
    }
    
    /**
     * @return true if this is an undeployment strategy (pu is undeploying)
     */
    protected abstract boolean isUndeploying();

    protected void machineProvisioningInProgressEvent(MachinesSlaEnforcementInProgressException e) {
        if (!machineProvisioningInProgressEventRaised) {
            machineProvisioningCompletedEventRaised = false;
            machineProvisioningInProgressEventRaised = true;
            boolean isComplete = false;
            eventQueue.enqueu(
                    new ElasticMachineProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        } 
                    
        if (e instanceof SlaEnforcementFailure) {
            eventQueue.enqueu(
                 new ElasticMachineProvisioningFailureEvent(e.getMessage(), ((SlaEnforcementFailure)e).getAffectedProcessingUnits()));
        }
    }
    
    protected void containerProvisioningCompletedEvent(GridServiceContainer[] containers) {

        if (!containerProvisioningCompletedEventRaised) {
            containerProvisioningCompletedEventRaised = true;
            containerProvisioningInProgressEventRaised = false;

            boolean isComplete = true;
            eventQueue.enqueu(
                    new ElasticGridServiceContainerProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        }
    }
    
    protected void containerProvisioningInProgressEvent(GridServiceContainer[] containers, ContainersSlaEnforcementInProgressException e) {

        if (!containerProvisioningInProgressEventRaised) {
            containerProvisioningCompletedEventRaised = false;
            containerProvisioningInProgressEventRaised = true;
            boolean isComplete = false;
            eventQueue.enqueu(
                    new ElasticGridServiceContainerProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        } 
                    
        if (e instanceof SlaEnforcementFailure) {
            eventQueue.enqueu(
                 new ElasticGridServiceContainerProvisioningFailureEvent(e.getMessage(), ((SlaEnforcementFailure)e).getAffectedProcessingUnits()));
        }
    }
    
    protected void puInstanceProvisioningCompletedEvent(ProcessingUnit pu) {
        
        if (!puProvisioningCompletedEventRaised) {
            puProvisioningCompletedEventRaised = true;
            puProvisioningInProgressEventRaised = false;

            boolean isComplete = true;
            eventQueue.enqueu(
                    new ElasticProcessingUnitInstanceProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        }
    }
    
    protected void puInstanceProvisioningInProgressEvent(ProcessingUnit pu, RebalancingSlaEnforcementInProgressException e) {
        if (!puProvisioningInProgressEventRaised) {
            puProvisioningCompletedEventRaised = false;
            puProvisioningInProgressEventRaised = true;
            boolean isComplete = false;
            eventQueue.enqueu(
                    new ElasticProcessingUnitInstanceProvisioningProgressChangedEvent(isComplete, isUndeploying(), getProcessingUnit().getName()));
        }
        if (e instanceof SlaEnforcementFailure) {
            eventQueue.enqueu(
                 new ElasticProcessingUnitInstanceProvisioningFailureEvent(e.getMessage(), ((SlaEnforcementFailure)e).getAffectedProcessingUnits()));
        }
    }
}
