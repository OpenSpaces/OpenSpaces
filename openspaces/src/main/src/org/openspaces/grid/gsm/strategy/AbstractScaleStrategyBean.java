package org.openspaces.grid.gsm.strategy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.alert.InternalAlertManager;
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
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.isolation.DedicatedMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.SharedMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementException;
import org.openspaces.grid.gsm.strategy.ProvisionedMachinesCache.AgentsNotYetDiscoveredException;

public abstract class AbstractScaleStrategyBean implements 
    ElasticMachineProvisioningAware ,
    ProcessingUnitAware,
    ScaleStrategyBean,
    Bean,
    Runnable{
    
    private static final int MAX_NUMBER_OF_MACHINES = 1000; // a very large number representing max number of machines per pu, but that would not overflow when multiplied by container capacity in MB
    
    private static final String MACHINES_ALERT_GROUP_UID_PREFIX = "3BA87E89-449A-4abc-A632-4732246A9EE4";
    private static final String REBALANCING_ALERT_GROUP_UID_PREFIX  = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String CONTAINERS_ALERT_GROUP_UID_PREFIX  = "47A94111-5665-4214-9F7A-2962D998DD12";

    private static final String MACHINES_ALERT_NAME = "Machine Provisioning Alert";
    private static final String CONTAINERS_ALERT_NAME = "Container Provisioning Alert";
    private static final String REBALANCING_ALERT_NAME = "Processing Unit Deployment and Rebalancing Alert";
    
    
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

    public long getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
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
    
    private void triggerAlert(AlertSeverity severity, AlertStatus status, String alertGroupUidPrefix, String alertName, String alertDescription) {
        
        String groupUid = alertGroupUidPrefix + "-" + getProcessingUnit().getName();
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        boolean filterAlert = 
                 alertsByGroupUid.length > 0 && 
                 alertsByGroupUid[0].getSeverity().equals(severity) &&
                 alertsByGroupUid[0].getStatus().equals(status) &&
                 alertsByGroupUid[0].getName().equals(alertName) &&
                 alertsByGroupUid[0].getDescription().equals(alertDescription);

        if (!filterAlert) {
            AlertFactory alertFactory = new AlertFactory();
            alertFactory.name(alertName);
            alertFactory.description(alertDescription);
            alertFactory.severity(severity);    
            alertFactory.status(status);
            alertFactory.componentUid(getProcessingUnit().getName());
            alertFactory.groupUid(groupUid);
            getAdmin().getAlertManager().triggerAlert(alertFactory.toAlert());
            
            if (getLogger().isInfoEnabled()) {
                getLogger().info(alertDescription);
            }
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
        catch (SlaEnforcementEndpointDestroyedException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("AdminService was destroyed",e);
            }
        }
        catch (AgentsNotYetDiscoveredException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Existing agents not discovered yet",e);
            }
        }
        catch (SlaEnforcementException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("SLA has not been reached",e);
            }
        }
        catch (AdminException e) {
            getLogger().warn("Unhandled AdminException",e);
        }
        catch (Exception e) {
            getLogger().error("Unhandled Exception",e);
        }
        finally {
            isScaleInProgress = isException;
        }
    }

    protected abstract void enforceSla() throws SlaEnforcementException;
    
    protected void raiseMachinesAlert(SlaEnforcementException e) {
        //TODO: Add alert specific properties
        //TODO: Add inner inner exception type and message
        triggerMachinesAlert(AlertStatus.RAISED, e.getMessage());
    }
    
    protected void resolveMachinesAlert(String alertDescription) {
        triggerMachinesAlert(AlertStatus.RESOLVED, alertDescription);
    }
    
    private void triggerMachinesAlert(AlertStatus status, String alertDescription) {
        triggerAlert(AlertSeverity.WARNING, status, MACHINES_ALERT_GROUP_UID_PREFIX, MACHINES_ALERT_NAME, alertDescription);
    }
    
    protected void raiseContainersAlert(SlaEnforcementException e) {
        //TODO: Add alert specific properties
        //TODO: Add inner inner exception type and message
        triggerContainersAlert(AlertStatus.RAISED, e.getMessage());
    }
    
    protected void resolveContainersAlert(String alertDescription) {
        triggerContainersAlert(AlertStatus.RESOLVED, alertDescription);
    }
    
    private void triggerContainersAlert(AlertStatus status, String alertDescription) {
        triggerAlert(AlertSeverity.WARNING, status, CONTAINERS_ALERT_GROUP_UID_PREFIX, CONTAINERS_ALERT_NAME, alertDescription);
    }
    
    protected void raiseRebalancingAlert(SlaEnforcementException e) {
        //TODO: Add alert specific properties
        //TODO: Add inner inner exception type and message
        triggerRebalancingAlert(AlertStatus.RAISED, e.getMessage());
    }
    
    protected void resolveRebalancingAlert(String alertDescription) {
        triggerRebalancingAlert(AlertStatus.RESOLVED, alertDescription);
    }
    
    private void triggerRebalancingAlert(AlertStatus status, String alertDescription) {
        triggerAlert(AlertSeverity.WARNING, status, REBALANCING_ALERT_GROUP_UID_PREFIX, REBALANCING_ALERT_NAME, alertDescription);
    }

    public boolean isScaleInProgress() {
        return isScaleInProgress;
    }
}
