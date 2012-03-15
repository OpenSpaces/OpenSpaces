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
package org.openspaces.admin.internal.pu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jini.rio.core.RequiredDependencies;
import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.application.InternalApplication;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.internal.pu.events.DefaultBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.ProvisionStatus;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailure;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.space.Space;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.support.RequiredDependenciesCommandLineParser;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.requirement.Requirement;
import org.openspaces.pu.sla.requirement.ZoneRequirement;

import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.internal.utils.StringUtils;

/**
 * @author kimchy
 */
public class DefaultProcessingUnit implements InternalProcessingUnit {

    private final InternalProcessingUnits processingUnits;

    private final InternalAdmin admin;

    private final String name;
    
    private final String simpleName;

    private volatile int numberOfInstances;

    private volatile int numberOfBackups;

    private final BeanLevelProperties beanLevelProperties;

    private final SLA sla;
    
    private final Map<String, String> elasticProperties;

    private volatile DeploymentStatus deploymentStatus = DeploymentStatus.NA;

    private volatile GridServiceManager managingGridServiceManager;

    private final Map<String, GridServiceManager> backupGridServiceManagers = new ConcurrentHashMap<String, GridServiceManager>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<Integer, ProcessingUnitPartition> processingUnitPartitions = new ConcurrentHashMap<Integer, ProcessingUnitPartition>();

    private final ConcurrentMap<String, Space> spaces = new ConcurrentHashMap<String, Space>();

    private final InternalManagingGridServiceManagerChangedEventManager managingGridServiceManagerChangedEventManager;

    private final InternalBackupGridServiceManagerChangedEventManager backupGridServiceManagerChangedEventManager;

    private final InternalProcessingUnitStatusChangedEventManager processingUnitStatusChangedEventManager;

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    private final InternalProcessingUnitSpaceCorrelatedEventManager spaceCorrelatedEventManager;

    private final InternalProcessingUnitInstanceStatisticsChangedEventManager processingUnitInstanceStatisticsChangedEventManager;

    //map processing unit instance name to it's provision status (mapping is kept even if instance is removed)
    private final Map<String, InternalProvisionStatusHolder> provisionStatus = new HashMap<String, InternalProvisionStatusHolder>();
    private final InternalProcessingUnitInstanceProvisionStatusChangedEventManager processingUnitInstanceProvisionStatusChangedEventManager;
    
    private final InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
    
    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private volatile boolean scheduledStatisticsMonitor = false;

    private final ProcessingUnitType processingUnitType;

    /**
     * @Deprecated since 8.0.6 use app.deploy(puDeployment) or gsm.deploy(new ApplicationDeployment(appName,puDeployment) instead.
     */
    private static final String APPLICATION_NAME_CONTEXT_PROPERTY = "com.gs.application";
    
    /**
     * @Deprecated since 8.0.6 use gsm.deploy(puDeployment.addDependency("otherPU")) instead.
     */
    private static final String APPLICATION_DEPENDENCIES_CONTEXT_PROPERTY = "com.gs.application.dependsOn";

	private final String applicationName;

    private volatile InternalApplication application;
    
    private final InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies;
    
    public DefaultProcessingUnit(InternalAdmin admin, InternalProcessingUnits processingUnits, PUDetails details) {
        this.admin = admin;
        this.processingUnits = processingUnits;
        this.name = details.getName();
        this.numberOfInstances = details.getNumberOfInstances();
        this.numberOfBackups = details.getNumberOfBackups();
        
        ProcessingUnitType type = ProcessingUnitType.UNKNOWN;
        try {
            type = ProcessingUnitType.valueOf(details.getType());
        }catch(Exception e) {
            //ignore
        }
        this.processingUnitType = type;
        
        this.elasticProperties = details.getElasticProperties();
        
        this.dependencies = new DefaultProcessingUnitDependencies();
        
        RequiredDependencies instanceDeploymentDependencies = details.getInstanceDeploymentDependencies();
        if (instanceDeploymentDependencies != null) {
            dependencies.addDetailedDependenciesByCommandLineOption(RequiredDependenciesCommandLineParser.INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME,instanceDeploymentDependencies);
        }
        
        RequiredDependencies instanceStartDependencies = details.getInstanceStartDependencies();
        if (instanceStartDependencies != null) {
            dependencies.addDetailedDependenciesByCommandLineOption(RequiredDependenciesCommandLineParser.INSTANCE_START_REQUIRED_DEPENDENCIES_PARAMETER_NAME,instanceStartDependencies);
        }
                
        try {
            this.beanLevelProperties = (BeanLevelProperties) details.getBeanLevelProperties().get();
        } catch (Exception e) {
            throw new AdminException("Failed to get bean level properties", e);
        }
        
        
        if (details.getApplicationName() != null) {
            applicationName = details.getApplicationName();
        }
        else {
            //Deprecated
            applicationName = getBeanLevelProperties().getContextProperties().getProperty(APPLICATION_NAME_CONTEXT_PROPERTY);
        }

        //initialize simpleName
        if( applicationName != null && applicationName.trim().length() > 0  ){
            String applNamePrefix = applicationName + ".";
            if( name.startsWith( applNamePrefix ) ){
                simpleName = name.substring( applNamePrefix.length() );
            }
            else{
                simpleName = name;
            }
        }
        else{
            simpleName = name;
        }
        
    
        try {
            this.sla = (SLA) details.getSla().get();
        } catch (Exception e) {
            throw new AdminException("Failed to get sla", e);
        }

        if (numberOfBackups == 0) {
            // if we have no backup, its actually just a "single partition"
            processingUnitPartitions.put(0, new DefaultProcessingUnitPartition(this, 0));
        } else {
            for (int i = 0; i < numberOfInstances; i++) {
                processingUnitPartitions.put(i, new DefaultProcessingUnitPartition(this, i));
            }
        }

        this.managingGridServiceManagerChangedEventManager = new DefaultManagingGridServiceManagerChangedEventManager(admin, this);
        this.backupGridServiceManagerChangedEventManager = new DefaultBackupGridServiceManagerChangedEventManager(admin, this);
        this.processingUnitStatusChangedEventManager = new DefaultProcessingUnitStatusChangedEventManager(admin, this);
        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);
        this.spaceCorrelatedEventManager = new DefaultProcessingUnitSpaceCorrelatedEventManager(this);
        this.processingUnitInstanceStatisticsChangedEventManager = new DefaultProcessingUnitInstanceStatisticsChangedEventManager(admin);
        
        this.processingUnitInstanceProvisionStatusChangedEventManager = new DefaultProcessingUnitInstanceProvisionStatusChangedEventManager(admin, this);
        this.processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager = new DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(admin, this);
    }

    @Override
    public ProcessingUnits getProcessingUnits() {
        return this.processingUnits;
    }

    @Override
    public Admin getAdmin() {
        return this.admin;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BeanLevelProperties getBeanLevelProperties() {
        return beanLevelProperties;
    }
    
    @Override
    public ProcessingUnitType getType() {
        return processingUnitType;
    }

    @Override
    public ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged() {
        return this.managingGridServiceManagerChangedEventManager;
    }

    @Override
    public BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged() {
        return this.backupGridServiceManagerChangedEventManager;
    }

    @Override
    public Space getSpace() {
        Iterator<Space> it = spaces.values().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public Space[] getSpaces() {
        return this.spaces.values().toArray(new Space[0]);
    }

    @Override
    public void addEmbeddedSpace(Space space) {
        assertStateChangesPermitted();
        Space existingSpace = spaces.putIfAbsent(space.getName(), space);
        if (existingSpace == null) {
            spaceCorrelatedEventManager.processingUnitSpaceCorrelated(new ProcessingUnitSpaceCorrelatedEvent(space, this));
        }
    }
    
    @Override
    public Map<String, String> getElasticProperties() {
        return this.elasticProperties;
    }

    @Override
    public ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged() {
        return this.processingUnitStatusChangedEventManager;
    }

    @Override
    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return this.processingUnitInstanceAddedEventManager;
    }

    @Override
    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return this.processingUnitInstanceRemovedEventManager;
    }

    @Override
    public ProcessingUnitSpaceCorrelatedEventManager getSpaceCorrelated() {
        return this.spaceCorrelatedEventManager;
    }

    @Override
    public void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().add(eventListener);
        getProcessingUnitInstanceRemoved().add(eventListener);
    }

    @Override
    public void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().remove(eventListener);
        getProcessingUnitInstanceRemoved().remove(eventListener);
    }

    @Override
    public int getNumberOfInstances() {
        return this.numberOfInstances;
    }

    @Override
    public void setNumberOfInstances(int numberOfInstances) {
        assertStateChangesPermitted();
        this.numberOfInstances = numberOfInstances;
    }

    @Override
    public int getNumberOfBackups() {
        return this.numberOfBackups;
    }

    @Override
    public void setNumberOfBackups(int numberOfBackups) {
        assertStateChangesPermitted();
        this.numberOfBackups = numberOfBackups;
    }

    @Override
    public int getTotalNumberOfInstances() {
        return getNumberOfInstances() * (getNumberOfBackups() + 1);
    }

    @Override
    public int getMaxInstancesPerVM() {
        return sla.getMaxInstancesPerVM();
    }

    @Override
    public int getMaxInstancesPerMachine() {
        return sla.getMaxInstancesPerMachine();
    }

    @Override
    public String getClusterSchema(){
        return sla.getClusterSchema();
    }
    
    @Override
    public Map<String, Integer> getMaxInstancesPerZone() {
        return Collections.unmodifiableMap(sla.getMaxInstancesPerZone());
    }

    @Override
    public String[] getRequiredZones() {
        ArrayList<String> zones = new ArrayList<String>();
        for (Requirement req : sla.getRequirements()) {
            if (req instanceof ZoneRequirement) {
                zones.add(((ZoneRequirement) req).getZone());
            }
        }
        return zones.toArray(new String[zones.size()]);
    }

    @Override
    public DeploymentStatus getStatus() {
        return this.deploymentStatus;
    }

    @Override
    public boolean waitFor(int numberOfProcessingUnitInstances) {
        return waitFor(numberOfProcessingUnitInstances, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                latch.countDown();
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    @Override
    public Space waitForSpace() {
        return waitForSpace(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Space waitForSpace(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Space> ref = new AtomicReference<Space>();
        ProcessingUnitSpaceCorrelatedEventListener correlated = new ProcessingUnitSpaceCorrelatedEventListener() {
            public void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event) {
                ref.set(event.getSpace());
                latch.countDown();
            }
        };
        getSpaceCorrelated().add(correlated);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getSpaceCorrelated().remove(correlated);
        }
    }

    @Override
    public GridServiceManager waitForManaged() {
        return waitForManaged(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public GridServiceManager waitForManaged(long timeout, TimeUnit timeUnit) {
        if (isManaged()) {
            return managingGridServiceManager;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        ManagingGridServiceManagerChangedEventListener listener = new ManagingGridServiceManagerChangedEventListener() {
            public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
                ref.set(event.getNewGridServiceManager());
                latch.countDown();
            }
        };
        getManagingGridServiceManagerChanged().add(listener);
        if (isManaged()) {
            getManagingGridServiceManagerChanged().remove(listener);
            return managingGridServiceManager;
        }
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getManagingGridServiceManagerChanged().remove(listener);
        }
    }

    @Override
    public boolean canIncrementInstance() {
        return getSpaces().length == 0;
    }

    @Override
    public boolean canDecrementInstance() {
        return getSpaces().length == 0;
    }

    @Override
    public void incrementInstance() {
        if (!isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) managingGridServiceManager).incrementInstance(this);
    }

    @Override
    public void decrementInstance() {
        Iterator<ProcessingUnitInstance> it = iterator();
        if (it.hasNext()) {
            ((InternalGridServiceManager) managingGridServiceManager).decrementInstance(it.next());
        }
    }

    @Override
    public GridServiceManager getManagingGridServiceManager() {
        return this.managingGridServiceManager;
    }

    @Override
    public GridServiceManager[] getBackupGridServiceManagers() {
        return this.backupGridServiceManagers.values().toArray(new GridServiceManager[0]);
    }

    @Override
    public boolean isManaged() {
        return managingGridServiceManager != null;
    }

    @Override
    public GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID) {
        return backupGridServiceManagers.get(gridServiceManagerUID);
    }

    @Override
    public boolean undeployAndWait(long timeout, TimeUnit timeUnit) {
        return ((InternalGridServiceManagers)admin.getGridServiceManagers()).undeployProcessingUnitsAndWait(
                new ProcessingUnit[] {this}, 
                timeout, timeUnit);
    }
    
    @Override
    public void undeployAndWait() {
        undeployAndWait(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    @Override
    public void undeploy() {
        if (!isManaged()) {
            throw new AdminException("No managing GSM to undeploy from");
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitRemovedEventListener listener = new ProcessingUnitRemovedEventListener() {
            public void processingUnitRemoved(ProcessingUnit processingUnit) {
                if (getName().equals(processingUnit.getName())) {
                    latch.countDown();
                }
            }
        };
        
        getProcessingUnits().getProcessingUnitRemoved().add(listener);
        try {
            ((InternalGridServiceManager) managingGridServiceManager).undeployProcessingUnit(getName());
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new AdminException("Failed to undeploy", e);
            }
        }finally {
            getProcessingUnits().getProcessingUnitRemoved().remove(listener);
        }
    }

    @Override
    public void setManagingGridServiceManager(GridServiceManager gridServiceManager) {
        assertStateChangesPermitted();
        this.managingGridServiceManager = gridServiceManager;
    }
    
    @Override
    public void addManagingGridServiceManager(GridServiceManager gridServiceManager) {
        assertStateChangesPermitted();
        final GridServiceManager previousManaging = (gridServiceManager == this.managingGridServiceManager) ? null : this.managingGridServiceManager;
        final GridServiceManager newManaging = gridServiceManager;

        this.managingGridServiceManager = gridServiceManager;
        ManagingGridServiceManagerChangedEvent event = new ManagingGridServiceManagerChangedEvent(this, newManaging, previousManaging);
        managingGridServiceManagerChangedEventManager.processingUnitManagingGridServiceManagerChanged(event);
        ((InternalManagingGridServiceManagerChangedEventManager) processingUnits.getManagingGridServiceManagerChanged()).processingUnitManagingGridServiceManagerChanged(event);
    }

    @Override
    public void addBackupGridServiceManager(final GridServiceManager backupGridServiceManager) {
        assertStateChangesPermitted();
        GridServiceManager gridServiceManager = this.backupGridServiceManagers.put(backupGridServiceManager.getUid(), backupGridServiceManager);
        if (gridServiceManager == null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.ADDED, backupGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    @Override
    public void removeBackupGridServiceManager(String gsmUID) {
        assertStateChangesPermitted();
        final GridServiceManager existingGridServiceManager = backupGridServiceManagers.remove(gsmUID);
        if (existingGridServiceManager != null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.REMOVED, existingGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    @Override
    public boolean setStatus(int statusCode) {
        assertStateChangesPermitted();
        DeploymentStatus tempStatus;
        switch (statusCode) {
            case 0:
                tempStatus = DeploymentStatus.UNDEPLOYED;
                break;
            case 1:
                tempStatus = DeploymentStatus.SCHEDULED;
                break;
            case 2:
                tempStatus = DeploymentStatus.DEPLOYED;
                break;
            case 3:
                tempStatus = DeploymentStatus.BROKEN;
                break;
            case 4:
                tempStatus = DeploymentStatus.COMPROMISED;
                break;
            case 5:
                tempStatus = DeploymentStatus.INTACT;
                break;
            default:
                throw new IllegalStateException("No status match");
        }
        if (tempStatus != deploymentStatus) {
            ProcessingUnitStatusChangedEvent event = new ProcessingUnitStatusChangedEvent(this, deploymentStatus, tempStatus);
            processingUnitStatusChangedEventManager.processingUnitStatusChanged(event);
            ((InternalProcessingUnitStatusChangedEventManager) processingUnits.getProcessingUnitStatusChanged()).processingUnitStatusChanged(event);
            deploymentStatus = tempStatus;
            return true;
        }
        deploymentStatus = tempStatus;
        return false;
    }

    @Override
    public Iterator<ProcessingUnitInstance> iterator() {
        return Collections.unmodifiableCollection(processingUnitInstances.values()).iterator();
    }

    @Override
    public ProcessingUnitInstance[] getInstances() {
        return processingUnitInstances.values().toArray(new ProcessingUnitInstance[0]);
    }

    @Override
    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return getInstances();
    }

    @Override
    public ProcessingUnitPartition[] getPartitions() {
        return processingUnitPartitions.values().toArray(new ProcessingUnitPartition[0]);
    }

    @Override
    public ProcessingUnitPartition getPartition(int partitionId) {
        return processingUnitPartitions.get(partitionId);
    }

    @Override
    public void addProcessingUnitInstance(final ProcessingUnitInstance processingUnitInstance) {
        assertStateChangesPermitted();
        final ProcessingUnitInstance existingProcessingUnitInstance = processingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
        InternalProcessingUnitPartition partition = getPartition(processingUnitInstance);
        partition.addProcessingUnitInstance(processingUnitInstance);
        ((InternalProcessingUnitInstance) processingUnitInstance).setProcessingUnitPartition(partition);

        // handle events
        if (existingProcessingUnitInstance == null) {
            processingUnitInstance.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
            processingUnitInstance.setStatisticsHistorySize(statisticsHistorySize);
            if (isMonitoring()) {
                admin.raiseEvent(this, new Runnable() {
                    public void run() {
                        processingUnitInstance.startStatisticsMonitor();
                    }
                });
            }
            processingUnitInstanceAddedEventManager.processingUnitInstanceAdded(processingUnitInstance);
            ((InternalProcessingUnitInstanceAddedEventManager) processingUnits.getProcessingUnitInstanceAdded()).processingUnitInstanceAdded(processingUnitInstance);
            if (application != null) {
                ((InternalProcessingUnitInstanceAddedEventManager) application.getProcessingUnits().getProcessingUnitInstanceAdded()).processingUnitInstanceAdded(processingUnitInstance);
            }
        }
    }

    @Override
    public void removeProcessingUnitInstance(String uid) {
        final ProcessingUnitInstance processingUnitInstance = processingUnitInstances.remove(uid);
        if (processingUnitInstance != null) {
            processingUnitInstance.stopStatisticsMonitor();
            InternalProcessingUnitPartition partition = getPartition(processingUnitInstance);
            partition.removeProcessingUnitInstance(uid);

            processingUnitInstanceRemovedEventManager.processingUnitInstanceRemoved(processingUnitInstance);
            ((InternalProcessingUnitInstanceRemovedEventManager) processingUnits.getProcessingUnitInstanceRemoved()).processingUnitInstanceRemoved(processingUnitInstance);
            
            if (application != null) {
                ((InternalProcessingUnitInstanceRemovedEventManager) application.getProcessingUnits().getProcessingUnitInstanceRemoved()).processingUnitInstanceRemoved(processingUnitInstance);
            }
        }
    }

    private InternalProcessingUnitPartition getPartition(ProcessingUnitInstance processingUnitInstance) {
        InternalProcessingUnitPartition partition;
        if (numberOfBackups == 0) {
            partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(0));
        } else {
            partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(processingUnitInstance.getInstanceId() - 1));
        }
        return partition;
    }

    @Override
    public ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged() {
        return this.processingUnitInstanceStatisticsChangedEventManager;
    }

    @Override
    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.setStatisticsInterval(interval, timeUnit);
        }
    }

    @Override
    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.setStatisticsHistorySize(historySize);
        }
    }

    @Override
    public synchronized void startStatisticsMonitor() {
        scheduledStatisticsMonitor = true;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.startStatisticsMonitor();
        }
    }

    @Override
    public synchronized void stopStatisticsMonitor() {
        scheduledStatisticsMonitor = false;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.stopStatisticsMonitor();
        }
    }

    @Override
    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultProcessingUnit that = (DefaultProcessingUnit) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    @Override
    public void scale(ScaleStrategyConfig strategyConfig) {
  
        InternalGridServiceManager gsm = (InternalGridServiceManager)getManagingGridServiceManager();
        
        if (gsm == null) {
            throw new AdminException("Processing Unit " + getName() + " does not have an associated managing GSM");
        }
        
        if (admin.getElasticServiceManagers().getSize() != 1) {
            throw new AdminException("ESM server is not running.");
        }
        
        gsm.setProcessingUnitScaleStrategyConfig(
                this, 
                strategyConfig);
    }
    
    @Override
    public void scaleAndWait(ScaleStrategyConfig strategyConfig) {
        scaleAndWait(strategyConfig, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }
    
    @Override
    public boolean scaleAndWait(ScaleStrategyConfig strategyConfig, long timeout, TimeUnit timeunit) {
        long end = System.currentTimeMillis() + timeunit.toMillis(timeout);
        scale(strategyConfig);
        
        while (true) {
        
            ElasticServiceManager[] elasticManagers = admin.getElasticServiceManagers().getManagers();
            if (elasticManagers.length == 1) {
                InternalElasticServiceManager esm = (InternalElasticServiceManager) elasticManagers[0];
                // we use the nocache method since the local cache does not contain the strategyConfig
                // so the cache may relate to the old startegyConfig
                // see GS-9999 for more details
                if (esm.isManagingProcessingUnitAndScaleNotInProgressNoCache(this)) {
                    //done waiting
                    break;
                }
            }
        
            long sleepDuration = end - System.currentTimeMillis();
            if (sleepDuration <= 0) {
                //timeout
                return false;
            }
            
            try {
                Thread.sleep(Math.min(1000, sleepDuration));
            } catch (InterruptedException e) {
                throw new AdminException("scaleAndWait interrupted",e);
            }
        }
        return true;
    }

    @Deprecated
    public void setElasticProperties(Map<String,String> properties) {
        if (getManagingGridServiceManager() == null) {
            throw new AdminException("Processing Unit " + getName() + " does not have an associated managing GSM");
        }
        ((InternalGridServiceManager)getManagingGridServiceManager()).setProcessingUnitElasticProperties(this, properties);
        
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        assertStateChangesPermitted();
        this.application = (InternalApplication) application;
    }
    
    @Override
    public ScaleStrategyConfig getScaleStrategyConfig() {
        //TODO: Cache the scale config each time a change notification arrives.
        if (getManagingGridServiceManager() == null) {
            throw new AdminException("Processing Unit " + getName() + " does not have an associated managing GSM");
        }
        return ((InternalGridServiceManager)getManagingGridServiceManager()).getProcessingUnitScaleStrategyConfig(this);
    }
    
    @Override
    public boolean decrementPlannedInstances() {
        return ((InternalGridServiceManager) managingGridServiceManager).decrementPlannedInstances(this);
    }

    /**
     * This method is used by the webui.
     * Need to use getDependencies once Cloudify is migrated to use the dependencies API
     */
    @Override
    public String getApplicationDependencies() {
        List<String> orderedNames = new ArrayList<String>();
        
        //deprecated: Current way of configuring pu dependencies in Cloudify is by entering a groovy list of the pu names in the format "[a,b,c]"
        String deprecatedDependenciesValue = getBeanLevelProperties().getContextProperties().getProperty(APPLICATION_DEPENDENCIES_CONTEXT_PROPERTY);
        if (deprecatedDependenciesValue != null) {
            String trimmedDeprecatedDependenciesValue = deprecatedDependenciesValue.replace('[', ' ').replace(']', ' ').trim();
            String[] deprecatedDependencies = StringUtils.delimitedListToStringArray(trimmedDeprecatedDependenciesValue,",");
            for (String name : deprecatedDependencies) {
                if (!orderedNames.contains(name)) {
                    orderedNames.add(name);
                }
            }
        }
        
        // Admin API way for configuring pu dependencies
        for (String name : dependencies.getDeploymentDependencies().getRequiredProcessingUnitsNames()) {
            if (!orderedNames.contains(name)) {
                orderedNames.add(name);
            }
        }
        if (orderedNames.isEmpty()) {
            //backwards compatibility
            return "";
        }
        return "[" + StringUtils.collectionToCommaDelimitedString(orderedNames) +"]";
    }
    
    @Override
    public ProcessingUnitDependencies<ProcessingUnitDependency> getDependencies() {
        return dependencies;
    }
    
    @Override
    public void processProvisionEvent(ProvisionLifeCycleEvent provisionLifeCycleEvent) {
        ProvisionStatus newStatus;
        switch(provisionLifeCycleEvent.getStatus()) {
        case ProvisionLifeCycleEvent.ALLOCATION_ATTEMPT: 
            newStatus = ProvisionStatus.ATTEMPT;
            break;
        case ProvisionLifeCycleEvent.ALLOCATION_SUCCESS:
            newStatus = ProvisionStatus.SUCCESS;
            break;
        case ProvisionLifeCycleEvent.ALLOCATION_FAILURE:
            newStatus = ProvisionStatus.FAILURE;
            break;
        case ProvisionLifeCycleEvent.PENDING_ALLOCATION:
            newStatus = ProvisionStatus.PENDING;
            break;
        default:
            throw new IllegalStateException("unknown provision life-cycle status");
        }
        InternalProvisionStatusHolder provisionStatusHolder = provisionStatus.get(provisionLifeCycleEvent.getProcessingUnitInstanceName());
        if (provisionStatusHolder == null) {
            provisionStatusHolder = new InternalProvisionStatusHolder();
        }
        ProvisionStatus lastProvisionStatus = provisionStatusHolder.getNewProvisionStatus();
        if (newStatus != lastProvisionStatus) {
            String processingUnitInstanceName = provisionLifeCycleEvent.getProcessingUnitInstanceName();
            
            final String exceptionMessage = provisionLifeCycleEvent.getException();
            if (exceptionMessage != null) {
                boolean uninstantiable = provisionLifeCycleEvent.isUninstantiable();
                ProcessingUnitInstanceProvisionFailure failure = new ProcessingUnitInstanceProvisionFailure(this, provisionLifeCycleEvent.getGscServiceId(), exceptionMessage, uninstantiable);
                try {
                    Field field = newStatus.getClass().getDeclaredField("provisionFailure");
                    field.setAccessible(true);
                    field.set(newStatus, failure);
                } catch (Exception e) {
                    admin.getAdminLogger().warn("failed to set provision failure " + e);
                }
            }
            
            ProcessingUnitInstanceProvisionStatusChangedEvent event = new ProcessingUnitInstanceProvisionStatusChangedEvent(this, processingUnitInstanceName, lastProvisionStatus, newStatus, provisionLifeCycleEvent.getGscServiceId());
            
            provisionStatusHolder.setNewProvisionStatus(newStatus);
            provisionStatusHolder.setPrevProvisionStatus(lastProvisionStatus);
            provisionStatus.put(provisionLifeCycleEvent.getProcessingUnitInstanceName(), provisionStatusHolder);

            processingUnitInstanceProvisionStatusChangedEventManager.processingUnitInstanceProvisionStatusChanged(event);
            ((InternalProcessingUnitInstanceProvisionStatusChangedEventManager)getProcessingUnits().getProcessingUnitInstanceProvisionStatusChanged()).processingUnitInstanceProvisionStatusChanged(event);
        }
    }

    @Override
    public Map<String, InternalProvisionStatusHolder> getProvisionStatusPerInstance() {
        return Collections.unmodifiableMap(provisionStatus);
    }
    
    @Override
    public String getSimpleName() {
        return simpleName;
    }
    
    @Override
    public ProcessingUnitInstanceProvisionStatusChangedEventManager getProcessingUnitInstanceProvisionStatusChanged() {
        return this.processingUnitInstanceProvisionStatusChangedEventManager;
    }
    
    
    @Override
    public ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager getProcessingUnitInstanceMemberAliveIndicatorStatusChanged() {
        return this.processingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
    }
}
