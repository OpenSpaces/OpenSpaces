package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.machine.InternalMachines;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultMachineAddedEventManager implements InternalMachineAddedEventManager {

    private final InternalMachines machines;

    private final InternalAdmin admin;

    private final List<MachineAddedEventListener> machineAddedEventListeners = new CopyOnWriteArrayList<MachineAddedEventListener>();

    public DefaultMachineAddedEventManager(InternalMachines machines) {
        this.machines = machines;
        this.admin = (InternalAdmin) machines.getAdmin();
    }

    @Override
    public void machineAdded(final Machine machine) {
        for (final MachineAddedEventListener listener : machineAddedEventListeners) {
            admin.pushEventAsFirst(listener, new Runnable() {
                @Override
                public void run() {
                    listener.machineAdded(machine);
                }
            });
        }
    }

    @Override
    public void add(final MachineAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                @Override
                public void run() {
                    for (Machine machine : machines.getMachines()) {
                        eventListener.machineAdded(machine);
                    }
                }
            });
        }
        machineAddedEventListeners.add(eventListener);
    }

    @Override
    public void add(final MachineAddedEventListener eventListener) {
        add(eventListener, true);
    }

    @Override
    public void remove(MachineAddedEventListener eventListener) {
        machineAddedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureMachineAddedEventListener(eventListener));
        } else {
            add((MachineAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureMachineAddedEventListener(eventListener));
        } else {
            remove((MachineAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}