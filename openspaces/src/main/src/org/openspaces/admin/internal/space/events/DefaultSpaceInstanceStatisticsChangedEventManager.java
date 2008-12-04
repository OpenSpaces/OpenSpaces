package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceStatisticsChangedEventManager implements InternalSpaceInstanceStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<SpaceInstanceStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<SpaceInstanceStatisticsChangedEventListener>();

    public DefaultSpaceInstanceStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void spaceInstanceStatisticsChanged(final SpaceInstanceStatisticsChangedEvent event) {
        for (final SpaceInstanceStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceInstanceStatisticsChanged(event);
                }
            });
        }
    }

    public void add(SpaceInstanceStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceInstanceStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceInstanceStatisticsChangedEventListener(eventListener));
        } else {
            add((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceInstanceStatisticsChangedEventListener(eventListener));
        } else {
            remove((SpaceInstanceStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}