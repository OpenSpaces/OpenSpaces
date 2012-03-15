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
package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.machine.InternalMachines;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultMachineRemovedEventManager implements InternalMachineRemovedEventManager {

    private final InternalMachines machines;

    private final InternalAdmin admin;

    private final List<MachineRemovedEventListener> machineRemovedEventListeners = new CopyOnWriteArrayList<MachineRemovedEventListener>();

    public DefaultMachineRemovedEventManager(InternalMachines machines) {
        this.machines = machines;
        this.admin = (InternalAdmin) machines.getAdmin();
    }

    @Override
    public void machineRemoved(final Machine machine) {
        for (final MachineRemovedEventListener listener : machineRemovedEventListeners) {
            admin.pushEvent(listener, new Runnable() {
                @Override
                public void run() {
                    listener.machineRemoved(machine);
                }
            });
        }
    }

    @Override
    public void add(MachineRemovedEventListener eventListener) {
        machineRemovedEventListeners.add(eventListener);
    }

    @Override
    public void remove(MachineRemovedEventListener eventListener) {
        machineRemovedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureMachineRemovedEventListener(eventListener));
        } else {
            add((MachineRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureMachineRemovedEventListener(eventListener));
        } else {
            remove((MachineRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
