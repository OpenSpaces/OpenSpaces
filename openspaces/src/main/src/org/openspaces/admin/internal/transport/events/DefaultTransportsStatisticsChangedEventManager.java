package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportsStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultTransportsStatisticsChangedEventManager implements InternalTransportsStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<TransportsStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<TransportsStatisticsChangedEventListener>();

    public DefaultTransportsStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void transportsStatisticsChanged(final TransportsStatisticsChangedEvent event) {
        for (final TransportsStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.transportsStatisticsChanged(event);
                }
            });
        }
    }

    public void add(TransportsStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(TransportsStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureTransportsStatisticsChangedEventListener(eventListener));
        } else {
            add((TransportsStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureTransportsStatisticsChangedEventListener(eventListener));
        } else {
            remove((TransportsStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}