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
 * Grid Service Containers hold all the different {@link VirtualMachine}s that are currently
 * discoverted.
 *
 * <p>Provides simple means to get all the current containers, as well as as registering for
 * container lifecycle (added and removed) events.
 *
 * <p>Provides the ability to start a statistics monitor on all current virtual machines using
 * {@link #startStatisticsMonitor()}. Newly discovered virtual machines will automatically use
 * the statistics monitor as well.
 * 
 * @author kimchy
 */
public interface VirtualMachines extends AdminAware, Iterable<VirtualMachine>, StatisticsMonitor {

    VirtualMachine[] getVirtualMachines();

    VirtualMachinesDetails getDetails();

    VirtualMachinesStatistics getStatistics();

    VirtualMachine getVirtualMachineByUID(String uid);

    Map<String, VirtualMachine> getUids();

    int getSize();

    boolean isEmpty();

    void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    VirtualMachineAddedEventManager getVirtualMachineAdded();

    VirtualMachineRemovedEventManager getVirtualMachineRemoved();

    VirtualMachinesStatisticsChangedEventManager getStatisticsChanged();

    VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged();
}
