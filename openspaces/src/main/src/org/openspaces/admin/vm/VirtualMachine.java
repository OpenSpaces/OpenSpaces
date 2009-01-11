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

/**
 * @author kimchy
 */
public interface VirtualMachine extends MachineAware, StatisticsMonitor {

    String getUid();

    VirtualMachineDetails getDetails();

    VirtualMachineStatistics getStatistics();

    GridServiceAgent getGridServiceAgent();

    /**
     * Returns the grid service manager started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceManager getGridServiceManager();

    /**
     * Returns the grid service container started within this virtual machine.
     * Returns <code>null</code> if no grid service manager was started within it.
     */
    GridServiceContainer getGridServiceContainer();

    /**
     * Returns the processing unit instnaces started within this virtual machine.
     */
    ProcessingUnitInstance[] getProcessingUnitInstances();

    /**
     * Returns the space instnaces started within this virtual machine.
     */
    SpaceInstance[] getSpaceInstances();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    SpaceInstanceAddedEventManager getSpaceInstanceAdded();

    SpaceInstanceRemovedEventManager getSpaceInstanceRemoved();

    void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener);

    VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged();
}
