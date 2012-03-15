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
import org.openspaces.admin.internal.vm.InternalVirtualMachines;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineAddedEventManager implements InternalVirtualMachineAddedEventManager {

    private final InternalVirtualMachines virtualMachines;

    private final InternalAdmin admin;

    private final List<VirtualMachineAddedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachineAddedEventListener>();

    public DefaultVirtualMachineAddedEventManager(InternalVirtualMachines virtualMachines) {
        this.virtualMachines = virtualMachines;
        this.admin = (InternalAdmin) virtualMachines.getAdmin();
    }

    public void virtualMachineAdded(final VirtualMachine virtualMachine) {
        for (final VirtualMachineAddedEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.virtualMachineAdded(virtualMachine);
                }
            });
        }
    }

    public void add(final VirtualMachineAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (VirtualMachine virtualMachine : virtualMachines) {
                        eventListener.virtualMachineAdded(virtualMachine);
                    }
                }
            });
        }
        eventListeners.add(eventListener);
    }

    public void add(final VirtualMachineAddedEventListener eventListener) {
        add(eventListener, true);
    }

    public void remove(VirtualMachineAddedEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureVirtualMachineAddedEventListener(eventListener));
        } else {
            add((VirtualMachineAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureVirtualMachineAddedEventListener(eventListener));
        } else {
            remove((VirtualMachineAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
