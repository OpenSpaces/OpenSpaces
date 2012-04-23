/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.pu;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.space.Space;
import org.openspaces.core.properties.BeanLevelProperties;

/**
 * A processing unit holds one or more {@link org.openspaces.admin.pu.ProcessingUnitInstance}s.
 *
 * @author kimchy
 * @author itaif
 */
public interface ProcessingUnit extends Iterable<ProcessingUnitInstance>, AdminAware, StatisticsMonitor {

    /**
     * Returns the handle to all the different processing units.
     */
    ProcessingUnits getProcessingUnits();
    
    /**
     * Returns the name of the processing unit.
     */
    String getName();

    /**
     * Returns the number of required instances as defined in the processing unit's SLA.
     * If there are backups, it will only return the number of primary instances and not the
     * number of backup. To get the total number of instances please use the method {@link #getTotalNumberOfInstances()}.
     * Note that this method does not count the number of running instances, but rather the number of planned
     * instances for the processing unit. To count the number of active processing unit instances please use the method
     * {@link #getInstances()}.   
     */
    int getNumberOfInstances();

    /**
     * Returns the number of backups (if the topology is a backup one) per instance, as defined in the
     * processing unit's SLA. Note that this method does not return he number of running backup instances, but
     * rather the number of planned backup instances per primary.
     */
    int getNumberOfBackups();

    /**
     * Returns the total required number of instances as defined in the processing SLA.
     * If there are no backups, will return{@link #getNumberOfInstances()}. If there are backups,
     * will return {@link #getNumberOfInstances()} * ({@link #getNumberOfBackups()}  + 1)
     * Note that this method does not count the number of running instances, but rather the total number of planned
     * instances for the processing unit. To count the number of active processing unit instances please use the method
     * {@link #getInstances()}.  
     */
    int getTotalNumberOfInstances();

    /**
     * Returns the number of instances of this processing unit that can run within a VM.
     *
     * <p>In case of a partitioned with backup topology, it applies on a per partition level (meaning that a
     * primary and backup will not run on the same VM).
     *
     * <p>In case of a non backup based topology, it applies on the number of instances of the whole processing
     * unit that can run on the same VM).
     */
    int getMaxInstancesPerVM();

    /**
     * Returns the number of instances of this processing unit that can run within a Machine.
     *
     * <p>In case of a partitioned with backup topology, it applies on a per partition level (meaning that a
     * primary and backup will not run on the same Machine).
     *
     * <p>In case of a non backup based topology, it applies on the number of instances of the whole processing
     * unit that can run on the same Machine). 
     */
    int getMaxInstancesPerMachine();

    /**
     * Returns a map containing the zone name and the maximum number of instances for that zone.
     */
    Map<String, Integer> getMaxInstancesPerZone();

    /**
     * Returns the list of zones this processing units are required to run on. If there is more than
     * one zone, the processing unit can run on either of the zones.
     */
    String[] getRequiredZones();

    /**
     * Returns the deployment status of the processing unit.
     */
    DeploymentStatus getStatus();

    /**
     * Return the deploy time properties of the processing unit.
     */
    BeanLevelProperties getBeanLevelProperties();
    
    /**
     * Returns the type of processing unit: stateless, stateful, mirror, web.
     * @since 8.0.3
     */
    ProcessingUnitType getType();

    /**
     * Waits till at least the provided number of Processing Unit Instances are up.
     */
    boolean waitFor(int numberOfProcessingUnitInstances);

    /**
     * Waits till at least the provided number of Processing Unit Instances are up for the specified timeout.
     */
    boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    /**
     * Waits till an embedded Space is correlated with the processing unit.
     */
    Space waitForSpace();

    /**
     * Waits till an embedded Space is correlated with the processing unit for the specified timeout.
     */
    Space waitForSpace(long timeout, TimeUnit timeUnit);

    /**
     * Waits till there is a managing {@link org.openspaces.admin.gsm.GridServiceManager} for the processing unit.  
     */
    GridServiceManager waitForManaged();

    /**
     * Waits till there is a managing {@link org.openspaces.admin.gsm.GridServiceManager} for the processing unit
     * for the specified timeout.
     */
    GridServiceManager waitForManaged(long timeout, TimeUnit timeUnit);

    /**
     * Returns <code>true</code> if this processing unit allows to increment instances on it.
     */
    boolean canIncrementInstance();

    /**
     * Returns <code>true</code> if this processing unit allows to decrement instances on it.
     */
    boolean canDecrementInstance();

    /**
     * Will increment a processing unit instance.
     */
    void incrementInstance();

    /**
     * Will randomly decrement an instance from the processing units. For more fine
     * grained control see {@link ProcessingUnitInstance#decrement()}.
     */
    void decrementInstance();

    /**
     * Returns <code>true</code> if there is a managing GSM for it.
     */
    boolean isManaged();

    /**
     * Returns the managing (primary) GSM for the processing unit.
     */
    GridServiceManager getManagingGridServiceManager();

    /**
     * Returns the backup GSMs for the processing unit.
     */
    GridServiceManager[] getBackupGridServiceManagers();

    /**
     * Returns the backup GSM matching the provided UID.
     */
    GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID);

    /**
     * @see ProcessingUnit#undeployAndWait()
     * @see ProcessingUnit#undeployAndWait(long, TimeUnit) 
     */
    void undeploy();

    /**
     * Un-deploys the processing unit and waits until all instances have been undeployed.
     * In case of an Elastic processing unit, also waits for containers to shutdown.
     * 
     * <p>The undeployment process will wait indefinitely and return when all processing units have undeployed.
     * 
     * @see ProcessingUnit#undeployAndWait(long, TimeUnit)
     * @see ProcessingUnit#undeploy()
     * @since 8.0.5
     */
    void undeployAndWait();
    
    /**
     * Undeploys the processing unit and waits until all instances have been undeployed.
     * In case of an Elastic processing unit, it waits until all containers have been removed.
     * 
     * <p>The undeployment process will wait for the given timeout and return when all processing units have undeployed or timeout expired.
     * 
     * @return True if un-deploy completed successfully within the specified timeout. False if undeploy is still in progress.
     * @see ProcessingUnit#undeployAndWait()
     * @see ProcessingUnit#undeploy()
     * @since 8.0.5
     */
    boolean undeployAndWait(long timeout, TimeUnit timeunit);
    
    /**
     * Returns the (first) embedded space within a processing unit. Returns <code>null</code> if
     * no embedded space is defined within the processing unit or if no processing unit instance
     * has been added to the processing unit.
     */
    Space getSpace();

    /**
     * Returns all the embedded spaces within a processing unit. Returns an empty array if there
     * are no embedded spaces defined within the processing unit, or none has been associated with
     * the processing unit yet.
     */
    Space[] getSpaces();

    /**
     * Returns the processing unit instances currently discovered.
     */
    ProcessingUnitInstance[] getInstances();

    /**
     * Returns the processing unit partitions of this processing unit.
     */
    ProcessingUnitPartition[] getPartitions();

    /**
     * Returns a processing unit partition based on the specified partition id.
     */
    ProcessingUnitPartition getPartition(int partitionId);

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener}s.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener}s.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Adds a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Removes a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Returns an event manger allowing to listen for {@link org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent}s.
     */
    ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged();

    /**
     * Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent}s.
     */
    BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged();

    /**
     * Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent}s.
     */
    ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged();

    /**
     * Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent}s.
     */
    ProcessingUnitSpaceCorrelatedEventManager getSpaceCorrelated();

    /**
     * Returns a processing unit instance statistics change event manger allowing to register for
     * events of {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent}.
     *
     * <p>Note, in order to receive events, the virtual machines need to be in a "statistics" monitored
     * state.
     */
    ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged();
    
    /**
     * Returns an event manager allowing to register {@link ProcessingUnitInstanceProvisionStatusChangedEventListener}s.
     * @since 8.0.6
     */
    ProcessingUnitInstanceProvisionStatusChangedEventManager getProcessingUnitInstanceProvisionStatusChanged();
    
    /**
     * Returns an event manager allowing to register {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener}s.
     * @since 8.0.6
     */
    ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager getProcessingUnitInstanceMemberAliveIndicatorStatusChanged();
            
    /**
     * Modifies the processing unit scalability strategy.
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @param strategyConfig
     * 
     * @since 8.0
     * @see ProcessingUnit#scaleAndWait(ScaleStrategyConfig)
     * @see ProcessingUnit#scaleAndWait(ScaleStrategyConfig, long, TimeUnit)
     */
    void scale(ScaleStrategyConfig strategyConfig);
    
    /**
     * Modifies the processing unit scalability strategy and waits until scale is complete
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @param strategyConfig
     * 
     * @since 8.0.5
     * @see ProcessingUnit#scale(ScaleStrategyConfig)
     */
    void scaleAndWait(ScaleStrategyConfig strategyConfig);

    /**
     * Modifies the processing unit scalability strategy and waits until scale is complete
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @param strategyConfig
     * @return <code>false</code> if timeout occurred before scale operation has completed.
     * 
     * @since 8.0.5
     * @see ProcessingUnit#scale(ScaleStrategyConfig)
     */
    boolean scaleAndWait(ScaleStrategyConfig strategyConfig, long timeout, TimeUnit timeUnit);
    
    /**
     * @return the application that this processing unit is associated with or null if this
     *         processing unit is not part of an application
     * 
     * @since 8.0.3
     */
    Application getApplication();

    /**
     * @return the dependencies this processing unit has on other processing units.
     * @since 8.0.6
     */
    ProcessingUnitDependencies<ProcessingUnitDependency> getDependencies();
}
