package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachinesStatisticsChangedEventManager implements InternalVirtualMachinesStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<VirtualMachinesStatisticsChangedEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachinesStatisticsChangedEventListener>();

    public DefaultVirtualMachinesStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
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