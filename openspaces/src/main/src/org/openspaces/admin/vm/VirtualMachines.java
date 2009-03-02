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

package org.openspaces.admin.vm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventManager;

import java.util.Map;

/**
 * Virtual Machines hold all the different {@link VirtualMachine}s that are currently
 * discovered.
 *
 * <p>Provides simple means to get all the current virtual machines, as well as as registering for
 * virtual machine lifecycle (added and removed) events.
 *
 * <p>Provides the ability to start a statistics monitor on all current virtual machines using
 * {@link #startStatisticsMonitor()}. Newly discovered virtual machines will automatically use
 * the statistics monitor as well.
 *
 * @author kimchy
 */
public interface VirtualMachines extends AdminAware, Iterable<VirtualMachine>, StatisticsMonitor {

    /**
     * Returns the currently discovered virtual machines.
     */
    VirtualMachine[] getVirtualMachines();

    /**
     * Returns the aggregated details of all virtual machines.
     */
    VirtualMachinesDetails getDetails();

    /**
     * Returns the aggregated statistics of all virtual machines.
     */
    VirtualMachinesStatistics getStatistics();

    /**
     * Return a virtual machine by its uid.
     */
    VirtualMachine getVirtualMachineByUID(String uid);

    /**
     * Returns a map of virtual machines with the key as the uid.
     */
    Map<String, VirtualMachine> getUids();

    /**
     * Returns the number of virtual machines current discovered.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no virtual machines, <code>false</code> otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the virtual machine added event manager allowing to add and remove
     * {@link org.openspaces.admin.vm.events.VirtualMachineAddedEventListener}s.
     */
    VirtualMachineAddedEventManager getVirtualMachineAdded();

    /**
     * Returns the virtual machine removed event manager allowing to add and remove
     * {@link org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener}s.
     */
    VirtualMachineRemovedEventManager getVirtualMachineRemoved();

    /**
     * Allows to add a {@link VirtualMachineLifecycleEventListener}.
     */
    void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link VirtualMachineLifecycleEventListener}.
     */
    void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    /**
     * Returns a virtual machines statistics change event manager allowing to register for events
     * of {@link org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEvent}.
     *
     * <p>Note, in order to receive events, the virtual machines need to be in a "statistics" monitored
     * state.
     */
    VirtualMachinesStatisticsChangedEventManager getStatisticsChanged();

    /**
     * Returns a virtual machien statistics change event manger allowing to register for
     * events of {@link org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent}.
     *
     * <p>Note, in order to receive events, the virtual machines need to be in a "statistics" monitored
     * state.
     */
    VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged();
}
