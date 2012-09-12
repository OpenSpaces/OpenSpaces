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
package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatisticsChangedEventManager implements InternalVirtualMachineStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final VirtualMachine virtualMachine;

    private final VirtualMachines virtualMachines;

    private final List<VirtualMachineStatisticsChangedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachineStatisticsChangedEventListener>();

    public DefaultVirtualMachineStatisticsChangedEventManager(InternalAdmin admin, VirtualMachines virtualMachines) {
        this.admin = admin;
        this.virtualMachines = virtualMachines;
        this.virtualMachine = null;
    }

    public DefaultVirtualMachineStatisticsChangedEventManager(InternalAdmin admin, VirtualMachine virtualMachine) {
        this.admin = admin;
        this.virtualMachine = virtualMachine;
        this.virtualMachines = null;
    }

    public void virtualMachineStatisticsChanged(final VirtualMachineStatisticsChangedEvent event) {
        for (final VirtualMachineStatisticsChangedEventListener listener : eventListeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.virtualMachineStatisticsChanged(event);
                }
            });
        }
    }

    public void add(VirtualMachineStatisticsChangedEventListener eventListener) {
        add(eventListener, false);
    }

    public void add(final VirtualMachineStatisticsChangedEventListener eventListener, boolean withHistory) {
        if (withHistory) {
            List<VirtualMachine> vms = new ArrayList<VirtualMachine>();
            if (virtualMachines != null) {
                vms.addAll(Arrays.asList(virtualMachines.getVirtualMachines()));
            } else if (virtualMachine != null) {
                vms.add(virtualMachine);
            }
            for (final VirtualMachine vm : vms) {
                VirtualMachineStatistics stats = vm.getStatistics();
                if (!stats.isNA()) {
                    List<VirtualMachineStatistics> timeline = stats.getTimeline();
                    Collections.reverse(timeline);
                    for (final VirtualMachineStatistics virtualMachineStatistics : timeline) {
                        admin.raiseEvent(eventListener, new Runnable() {
                            public void run() {
                                eventListener.virtualMachineStatisticsChanged(new VirtualMachineStatisticsChangedEvent(vm, virtualMachineStatistics));
                            }
                        });
                    }
                }
            }
        }
        eventListeners.add(eventListener);
    }

    public void remove(VirtualMachineStatisticsChangedEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureVirtualMachineStatisticsChangedEventListener(eventListener));
        } else {
            add((VirtualMachineStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureVirtualMachineStatisticsChangedEventListener(eventListener));
        } else {
            remove((VirtualMachineStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
