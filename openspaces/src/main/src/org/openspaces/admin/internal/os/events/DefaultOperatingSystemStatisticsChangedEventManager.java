package org.openspaces.admin.internal.os.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemStatisticsChangedEventManager implements InternalOperatingSystemStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<OperatingSystemStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<OperatingSystemStatisticsChangedEventListener>();

    public DefaultOperatingSystemStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void operatingSystemStatisticsChanged(final OperatingSystemStatisticsChangedEvent event) {
        for (final OperatingSystemStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.operatingSystemStatisticsChanged(event);
                }
            });
        }
    }

    public void add(OperatingSystemStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(OperatingSystemStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureOperatingSystemStatisticsChangedEventListener(eventListener));
        } else {
            add((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureOperatingSystemStatisticsChangedEventListener(eventListener));
        } else {
            remove((OperatingSystemStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}