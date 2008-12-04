package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatisticsChangedEventManager implements InternalVirtualMachineStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<VirtualMachineStatisticsChangedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachineStatisticsChangedEventListener>();

    public DefaultVirtualMachineStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
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