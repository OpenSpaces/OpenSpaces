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
import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.vm.VirtualMachinesStatistics;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachinesStatisticsChangedEventManager implements InternalVirtualMachinesStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final VirtualMachines virtualMachines;

    private final List<VirtualMachinesStatisticsChangedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachinesStatisticsChangedEventListener>();

    public DefaultVirtualMachinesStatisticsChangedEventManager(InternalAdmin admin, VirtualMachines virtualMachines) {
        this.admin = admin;
        this.virtualMachines = virtualMachines;
    }

    public void virtualMachinesStatisticsChanged(final VirtualMachinesStatisticsChangedEvent event) {
        for (final VirtualMachinesStatisticsChangedEventListener listener : eventListeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.virtualMachinesStatisticsChanged(event);
                }
            });
        }
    }

    public void add(VirtualMachinesStatisticsChangedEventListener eventListener) {
        add(eventListener, false);
    }

    public void add(final VirtualMachinesStatisticsChangedEventListener eventListener, boolean withHistory) {
        if (withHistory) {
            VirtualMachinesStatistics stats = virtualMachines.getStatistics();
            if (!stats.isNA()) {
                List<VirtualMachinesStatistics> timeline = stats.getTimeline();
                Collections.reverse(timeline);
                for (final VirtualMachinesStatistics virtualMachineStatistics : timeline) {
                    admin.raiseEvent(eventListener, new Runnable() {
                        public void run() {
                            eventListener.virtualMachinesStatisticsChanged(new VirtualMachinesStatisticsChangedEvent(virtualMachines, virtualMachineStatistics));
                        }
                    });
                }
            }
        }
        eventListeners.add(eventListener);
    }

    public void remove(VirtualMachinesStatisticsChangedEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureVirtualMachinesStatisticsChangedEventListener(eventListener));
        } else {
            add((VirtualMachinesStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureVirtualMachinesStatisticsChangedEventListener(eventListener));
        } else {
            remove((VirtualMachinesStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
