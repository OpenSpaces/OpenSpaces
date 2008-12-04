package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.events.ReplicationStatusChangedEvent;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultReplicationStatusChangedEventManager implements InternalReplicationStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ReplicationStatusChangedEventListener> listeners = new CopyOnWriteArrayList<ReplicationStatusChangedEventListener>();

    public DefaultReplicationStatusChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void replicationStatusChanged(final ReplicationStatusChangedEvent event) {
        for (final ReplicationStatusChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.replicationStatusChanged(event);
                }
            });
        }
    }

    public void add(ReplicationStatusChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(ReplicationStatusChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureReplicationStatusChangedEventListener(eventListener));
        } else {
            add((ReplicationStatusChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureReplicationStatusChangedEventListener(eventListener));
        } else {
            remove((ReplicationStatusChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}