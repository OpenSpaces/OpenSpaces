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
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineRemovedEventManager implements InternalVirtualMachineRemovedEventManager {

    private final InternalVirtualMachines virtualMachines;

    private final InternalAdmin admin;

    private final List<VirtualMachineRemovedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachineRemovedEventListener>();

    public DefaultVirtualMachineRemovedEventManager(InternalVirtualMachines virtualMachines) {
        this.virtualMachines = virtualMachines;
        this.admin = (InternalAdmin) virtualMachines.getAdmin();
    }

    public void virtualMachineRemoved(final VirtualMachine virtualMachine) {
        for (final VirtualMachineRemovedEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.virtualMachineRemoved(virtualMachine);
                }
            });
        }
    }

    public void add(VirtualMachineRemovedEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void remove(VirtualMachineRemovedEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureVirtualMachineRemovedEventListener(eventListener));
        } else {
            add((VirtualMachineRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureVirtualMachineRemovedEventListener(eventListener));
        } else {
            remove((VirtualMachineRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
