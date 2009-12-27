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

package org.openspaces.admin.vm;

import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.machine.MachineAware;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.zone.ZoneAware;

/**
 * A virtual machine is a JVM that runs grid components.
 *
 * @author kimchy
 */
public interface VirtualMachine extends MachineAware, ZoneAware, StatisticsMonitor {

    /**
     * Returns the uid of the virtual machine.
     */
    String getUid();

    /**
     * Returns the details (non changeable information) of the virtual machine.
     */
    VirtualMachineDetails getDetails();

    /**
     * Returns the statistics of the virtual machine.
     */
    VirtualMachineStatistics getStatistics();

    /**
     * Runs GC on the virtual machine.
     */
    void runGc();

    /**
     * Returns the grid service agent started within this virtual machine.
     * Returns <code>null</code> if no grid service agent was started within it.
     */
    GridServiceAgent getGridServiceAgent();

    /**
     * Returns the grid service manager started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceManager getGridServiceManager();
    
    /**
     * Returns the elastic service manager started within this virtual machine.
     * Returns <code>null</code> if no elastic service manager was started within it.
     */
    ElasticServiceManager getElasticServiceManager();

    /**
     * Returns the grid service container started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceContainer getGridServiceContainer();

    /**
     * Returns the processing unit instances started within this virtual machine.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the space instances started within this virtual machine.
     */
    SpaceInstance[] getSpaceInstances();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener}s
     * for processing unit instances added on this virtual machine.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener}s
     * for processing unit instances removed on this virtual machine.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Adds an {@link ProcessingUnitInstanceLifecycleEventListener} allowing to be notified when a processing unit
     * instance was added or removed from this virtual machine.
     */
    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Removes an {@link ProcessingUnitInstanceLifecycleEventListener} allowing to be notified when a processing unit
     * instance was added or removed from this virtual machine.
     */
    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to register for {@link org.openspaces.admin.space.events.SpaceInstanceAddedEventListener}s
     * for space instances added on this virtual machine.
     */
    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    /**
     * Returns an event manager allowing to register for {@link org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener}s
     * for space instances added on this virtual machine.
     */
    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    /**
     * Adds an {@link SpaceInstanceLifecycleEventListener} allowing to be notified when a space
     * instance was added or removed from this virtual machine.
     */
    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Removes an {@link SpaceInstanceLifecycleEventListener} allowing to be notified when a space
     * instance was added or removed from this virtual machine.
     */
    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to register for {@link org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent}s.
     *
     * <p>Note, the events will be raised only when the {@link #startStatisticsMonitor()} is called.
     */
    VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged();
}
