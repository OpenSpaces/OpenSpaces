package org.openspaces.admin.internal.os.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemsStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsStatisticsChangedEventManager implements InternalOperatingSystemsStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<OperatingSystemsStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<OperatingSystemsStatisticsChangedEventListener>();

    public DefaultOperatingSystemsStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void operatingSystemsStatisticsChanged(final OperatingSystemsStatisticsChangedEvent event) {
        for (final OperatingSystemsStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.operatingSystemsStatisticsChanged(event);
                }
            });
        }
    }

    public void add(OperatingSystemsStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(OperatingSystemsStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureOperatingSystemsStatisticsChangedEventListener(eventListener));
        } else {
            add((OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureOperatingSystemsStatisticsChangedEventListener(eventListener));
        } else {
            remove((OperatingSystemsStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}