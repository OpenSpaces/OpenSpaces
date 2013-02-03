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
package org.openspaces.grid.gsm.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.monitor.event.EventsStore;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.gsc.events.DefaultElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.pu.elastic.events.DefaultElasticProcessingUnitScaleProgressChangedEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.events.ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.ElasticMachineProvisioningAware;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState.RecoveryState;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.UndeployInProgressException;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolationAware;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.DisconnectedFromLookupServiceException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;
import org.openspaces.grid.gsm.sla.exceptions.WrongNumberOfESMComponentsException;

public abstract class AbstractScaleStrategyBean implements 
    ElasticMachineProvisioningAware ,
    ProcessingUnitAware,
    ElasticScaleStrategyEventStorageAware,
    ScaleStrategyBean,
    ElasticProcessingUnitMachineIsolationAware,
    Bean,
    Runnable{
    
    private static final int MAX_NUMBER_OF_MACHINES = 1000; // a very large number representing max number of machines per pu, but that would not overflow when multiplied by container capacity in MB
    
    // injected 
    private InternalAdmin admin;
    private InternalProcessingUnit pu;
    private ProcessingUnitSchemaConfig schemaConfig;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private StringProperties properties;

    
    // created by afterPropertiesSet()
    private Log logger;
    private int minimumNumberOfMachines;    
    private ElasticProcessingUnitMachineIsolation isolation;
    private ScheduledFuture<?> scheduledTask;
        
    // state
    private ElasticMachineProvisioningDiscoveredMachinesCache provisionedMachines;
    private boolean isScaleInProgress;

    // events state 
    private ScaleStrategyProgressEventState machineProvisioningEventState;
    private ScaleStrategyProgressEventState agentProvisioningEventState;
    private ScaleStrategyProgressEventState containerProvisioningEventState;
    private ScaleStrategyProgressEventState puProvisioningEventState;
    private ScaleStrategyProgressEventState scaleEventState;
    private ScaleStrategyProgressEventState capacityPlanningEventState;
    
    private EventsStore eventsStore;

    private boolean discoveryQuiteMode;

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
        this.pu = (InternalProcessingUnit) pu;
    }


    protected InternalProcessingUnit getProcessingUnit() {
        return pu;
    }
    
    protected long getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    @Override
    public void setElasticProcessingUnitMachineIsolation(ElasticProcessingUnitMachineIsolation isolation) {
        this.isolation = isolation;
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
    public void setElasticScaleStrategyEventStorage(EventsStore eventQueue) {
        this.eventsStore = eventQueue;
    }
    
    protected void setMachineDiscoveryQuiteMode(boolean discoveryQuiteMode) {
        this.discoveryQuiteMode = discoveryQuiteMode;
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
        
        validateCorrectThread();
        
        if (machineProvisioning == null) {
            throw new IllegalStateException("machine provisioning cannot be null.");
        }
        
        if (schemaConfig == null) {
            throw new IllegalStateException("schemaConfig cannot be null.");
        }
    
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(this.getClass())),
                    pu);
        
        logger.info("properties: "+properties);
    
        machineProvisioningEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticMachineProvisioningProgressChangedEvent.class);
        agentProvisioningEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticGridServiceAgentProvisioningProgressChangedEvent.class);
        containerProvisioningEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticGridServiceContainerProvisioningProgressChangedEvent.class);
        puProvisioningEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticProcessingUnitInstanceProvisioningProgressChangedEvent.class);
        scaleEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticProcessingUnitScaleProgressChangedEvent.class);
        capacityPlanningEventState = new ScaleStrategyProgressEventState(eventsStore, isUndeploying(), pu.getName(), DefaultElasticAutoScalingProgressChangedEvent.class);
        
        minimumNumberOfMachines = calcMinimumNumberOfMachines();
        provisionedMachines = new ElasticMachineProvisioningDiscoveredMachinesCache(pu,machineProvisioning, discoveryQuiteMode, getPollingIntervalSeconds());
        
        isScaleInProgress = true;

        // Notice that we start the thread before derived implementation of #afterPropertiesSet
        // complete.
        // This is ok as long as #afterPropertiesSet is called on the same thread
        // as #run() would be called (which is the single thread admin)
        // InternalAdmin#assertStateChangesPermitted() validates just that
        validateCorrectThread();
        
        ElasticGridServiceAgentProvisioningProgressChangedEventListener agentEventListener = new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticGridServiceAgentProvisioningProgressChanged(
                    final ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                if (!(event instanceof InternalElasticProcessingUnitProgressChangedEvent)) {
                    throw new IllegalArgumentException("event must implement " + InternalElasticProcessingUnitProgressChangedEvent.class.getName());
                }
                admin.scheduleNonBlockingStateChange(new Runnable() {
                    
                    @Override
                    public void run() {
                        injectEventContext((InternalElasticProcessingUnitProgressChangedEvent) event);
                        agentProvisioningEventState.enqueuProvisioningInProgressEvent((InternalElasticProcessingUnitProgressChangedEvent) event);
                    }
                });
            }
        };
        machineProvisioning.setElasticGridServiceAgentProvisioningProgressEventListener(agentEventListener);
        
        ElasticMachineProvisioningProgressChangedEventListener machineEventListener = new ElasticMachineProvisioningProgressChangedEventListener() {
            
            @Override
            public void elasticMachineProvisioningProgressChanged(
                    final ElasticMachineProvisioningProgressChangedEvent event) {
                if (!(event instanceof InternalElasticProcessingUnitProgressChangedEvent)) {
                    throw new IllegalArgumentException("event must implement " + InternalElasticProcessingUnitProgressChangedEvent.class.getName());
                }
                admin.scheduleNonBlockingStateChange(new Runnable() {
                    @Override
                    public void run() {                       
                        injectEventContext((InternalElasticProcessingUnitProgressChangedEvent) event);
                        machineProvisioningEventState.enqueuProvisioningInProgressEvent((InternalElasticProcessingUnitProgressChangedEvent) event);
                    }
                });
            }
        };
        machineProvisioning.setElasticMachineProvisioningProgressChangedEventListener(machineEventListener);
        
        scheduledTask = 
            admin.scheduleWithFixedDelayNonBlockingStateChange(
                    this, 
                    0L, 
                    getPollingIntervalSeconds(), 
                    TimeUnit.SECONDS);
        
        logger.debug(pu.getName() + " is being monitored for SLA violations every " + getPollingIntervalSeconds() + " seconds");
    }

    private void injectEventContext(InternalElasticProcessingUnitProgressChangedEvent event) {
        event.setProcessingUnitName(pu.getName());
        event.setComplete(false);
        event.setUndeploying(AbstractScaleStrategyBean.this.isUndeploying());  
    }
    
    @Override
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

    protected DiscoveredMachinesCache getDiscoveredMachinesCache() {
        return provisionedMachines;
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

        if (!isScaleInProgress() && isUndeploying()) {
            // undeploy bean should run only once
            return;
        }
        
        try {
            recoverOnStartBeforeEnforceSLA();

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("enforcing SLA.");
            }
            
            
            enforceSla();
            
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("SLA enforced.");
            }
            
            ZonesConfig zones = null; // gsa zones not relevant for this context
            scaleEventState.enqueuProvisioningCompletedEvent(zones);
            isScaleInProgress = false;
        }
        catch (SlaEnforcementInProgressException e) {
            ZonesConfig zones = null; // gsa zones not relevant for this context
            scaleEventState.enqueuDefaultProvisioningInProgressEvent(e, zones);    
            
            isScaleInProgress = true;
        }
        catch (Throwable t) {
            getLogger().error("Unhandled Exception",t);
            ZonesConfig zones = null; // gsa zones not relevant for this context
            scaleEventState.enqueuDefaultProvisioningInProgressEvent(t, zones);
            isScaleInProgress = true;
        }
    }

    private void recoverOnStartBeforeEnforceSLA() throws SlaEnforcementInProgressException {
        try {
            validateCorrectThread();
            validateAtLeastOneLookupServiceDiscovered();
            validateOnlyOneESMRunning();
    
            RecoveryState recoveryState = getRecoveredStateOnEsmStart();
            if (recoveryState.equals(RecoveryState.RECOVERY_FAILED)) {
                List<ProcessingUnit> pusNotCompletedStateRecovery = new ArrayList<ProcessingUnit>();
                pusNotCompletedStateRecovery.add(getProcessingUnit());
                throw new SomeProcessingUnitsHaveNotCompletedStateRecoveryException(getProcessingUnit(), pusNotCompletedStateRecovery);
            }
            if (recoveryState.equals(RecoveryState.NOT_RECOVERED)) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("recovering state on ESM start.");
                }
                
                recoverStateOnEsmStart();
                
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("recovered state on ESM start.");
                }
                
                RecoveryState newRecoveryState = getRecoveredStateOnEsmStart();
                if(!getRecoveredStateOnEsmStart().equals(RecoveryState.RECOVERY_SUCCESS)) {
                    throw new IllegalStateException("PU " + getProcessingUnit().getName() + " recovery state is " + newRecoveryState + " instead of " + RecoveryState.RECOVERY_SUCCESS);
                }
            }
            validateAllProcessingUnitsRecoveredStateOnEsmStart();
        } catch (final SlaEnforcementInProgressException e) {
            //TODO: Fire event
            getLogger().info("SLA is not enforced",e);
            throw e;
        }
    }

    private void validateCorrectThread() {
        ((InternalAdmin)admin).assertStateChangesPermitted();
    }

    private RecoveryState getRecoveredStateOnEsmStart() {
        return getRecoveredStateOnEsmStart(pu);
    }

    protected abstract RecoveryState getRecoveredStateOnEsmStart(ProcessingUnit otherPu);
    protected abstract void recoverStateOnEsmStart() throws MachinesSlaEnforcementInProgressException, SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException, UndeployInProgressException;
    
        
    /**
     * Make sure the other PUs have updated their state, so their won't be race condition
     * on allocating discovered agents when enforcing sla.
     * @throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException 
     */
    protected void validateAllProcessingUnitsRecoveredStateOnEsmStart() throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException {
        
        List<ProcessingUnit> pusNotCompletedStateRecovery = new ArrayList<ProcessingUnit>();
                
        Admin admin = pu.getAdmin();
        
        for (ProcessingUnit otherPu : admin.getProcessingUnits()) {
            Map<String, String> elasticProperties = ((InternalProcessingUnit)otherPu).getElasticProperties();
            if (!elasticProperties.isEmpty() &&
                getRecoveredStateOnEsmStart(otherPu).equals(RecoveryState.NOT_RECOVERED)){   
                  // found an elastic PU that has not completed state recovery
                  pusNotCompletedStateRecovery.add(otherPu);
            }
        }

        if (!pusNotCompletedStateRecovery.isEmpty()) {
            throw new SomeProcessingUnitsHaveNotCompletedStateRecoveryException(getProcessingUnit(), pusNotCompletedStateRecovery);
        }   
    }

    private void validateAtLeastOneLookupServiceDiscovered() throws DisconnectedFromLookupServiceException {
        final int numberOfLookupServices= admin.getLookupServices().getSize();
        if (numberOfLookupServices == 0) {
            final DisconnectedFromLookupServiceException e = new DisconnectedFromLookupServiceException(this.getProcessingUnit(), admin.getLocators(), admin.getGroups());
            //eventually raises a machines alert. That's good enough
            ZonesConfig zones = null; // gsa zones not relevant for this error
            machineProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
            throw e;
        }
        
    }

    private void validateOnlyOneESMRunning() throws WrongNumberOfESMComponentsException {
        final int numberOfEsms = admin.getElasticServiceManagers().getSize();
        if (numberOfEsms != 1) {
            
            final WrongNumberOfESMComponentsException e = new WrongNumberOfESMComponentsException(numberOfEsms, this.getProcessingUnit().getName());
            //eventually raises a machines alert. That's good enough
            ZonesConfig zones = null; // gsa zones not relevant for this error
            machineProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
            throw e;
        }
    }

    protected abstract void enforceSla() throws SlaEnforcementInProgressException;

    /**
     * @return true if this is an undeployment strategy (pu is undeploying)
     */
    protected abstract boolean isUndeploying();

    
    public boolean isScaleInProgress() {
        return isScaleInProgress;
    }

    protected void agentProvisioningCompletedEvent(ZonesConfig zones) {
        agentProvisioningEventState.enqueuProvisioningCompletedEvent(zones);
    }
    
    protected void agentProvisioningInProgressEvent(GridServiceAgentSlaEnforcementInProgressException e, ZonesConfig zones) {
        agentProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
    }

    protected void machineProvisioningCompletedEvent(ZonesConfig zones) {
        machineProvisioningEventState.enqueuProvisioningCompletedEvent(zones);
    }
    
    protected void machineProvisioningInProgressEvent(MachinesSlaEnforcementInProgressException e, ZonesConfig zones) {
        machineProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
    }
    
    protected void containerProvisioningCompletedEvent() {
        ZonesConfig zones = null; // gsa zones not relevant for containers
        containerProvisioningEventState.enqueuProvisioningCompletedEvent(zones);
    }
    
    protected void containerProvisioningInProgressEvent(ContainersSlaEnforcementInProgressException e) {
        ZonesConfig zones = null; // gsa zones not relevant for containers
        containerProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
    }
    
    protected void puInstanceProvisioningCompletedEvent() {
        ZonesConfig zones = null; // gsa zones not relevant for containers
        puProvisioningEventState.enqueuProvisioningCompletedEvent(zones);
    }

    protected void puInstanceProvisioningInProgressEvent(RebalancingSlaEnforcementInProgressException e) {
        ZonesConfig zones = null; // gsa zones not relevant for containers
        puProvisioningEventState.enqueuProvisioningInProgressEvent(e, zones);
    }
    

    protected void capacityPlanningCompletedEvent(ZonesConfig zones) {
        capacityPlanningEventState.enqueuProvisioningCompletedEvent(zones);
    }

    protected void capacityPlanningInProgressEvent(AutoScalingSlaEnforcementInProgressException e, ZonesConfig zones) {
        capacityPlanningEventState.enqueuProvisioningInProgressEvent(e, zones);
    }

    public void capacityPlanningInProgressEvent(
            ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent event) {
        
        capacityPlanningEventState.enqueuProvisioningInProgressEvent(event);
    }
}
