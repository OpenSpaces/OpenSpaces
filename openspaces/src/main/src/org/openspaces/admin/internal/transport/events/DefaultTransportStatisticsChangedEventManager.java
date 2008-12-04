package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultTransportStatisticsChangedEventManager implements InternalTransportStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<TransportStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<TransportStatisticsChangedEventListener>();

    public DefaultTransportStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void transportStatisticsChanged(final TransportStatisticsChangedEvent event) {
        for (final TransportStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.transportStatisticsChanged(event);
                }
            });
        }
    }

    public void add(TransportStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(TransportStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureTransportStatisticsChangedEventListener(eventListener));
        } else {
            add((TransportStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureTransportStatisticsChangedEventListener(eventListener));
        } else {
            remove((TransportStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}