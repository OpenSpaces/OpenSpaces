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