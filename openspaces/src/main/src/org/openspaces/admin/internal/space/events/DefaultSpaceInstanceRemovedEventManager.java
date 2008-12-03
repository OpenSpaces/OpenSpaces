package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceRemovedEventManager implements InternalSpaceInstanceRemovedEventManager {

    private final InternalAdmin admin;

    private final List<SpaceInstanceRemovedEventListener> listeners = new CopyOnWriteArrayList<SpaceInstanceRemovedEventListener>();

    public DefaultSpaceInstanceRemovedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void spaceInstanceRemoved(final SpaceInstance spaceInstance) {
        for (final SpaceInstanceRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceInstanceRemoved(spaceInstance);
                }
            });
        }
    }

    public void add(final SpaceInstanceRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceInstanceRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureInstanceSpaceRemovedEventListener(eventListener));
        } else {
            add((SpaceInstanceRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureInstanceSpaceRemovedEventListener(eventListener));
        } else {
            remove((SpaceInstanceRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}