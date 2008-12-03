package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceModeChangedEventManager implements InternalSpaceModeChangedEventManager {

    private final InternalAdmin admin;

    private final List<SpaceModeChangedEventListener> listeners = new CopyOnWriteArrayList<SpaceModeChangedEventListener>();

    public DefaultSpaceModeChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void spaceModeChanged(final SpaceModeChangedEvent event) {
        for (final SpaceModeChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceModeChanged(event);
                }
            });
        }
    }

    public void add(SpaceModeChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceModeChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceModeChangedEventListener(eventListener));
        } else {
            add((SpaceModeChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceModeChangedEventListener(eventListener));
        } else {
            remove((SpaceModeChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}