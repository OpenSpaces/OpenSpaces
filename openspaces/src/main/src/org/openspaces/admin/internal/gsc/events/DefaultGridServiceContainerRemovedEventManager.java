package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainerRemovedEventManager implements InternalGridServiceContainerRemovedEventManager {

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalAdmin admin;

    private final List<GridServiceContainerRemovedEventListener> listeners = new CopyOnWriteArrayList<GridServiceContainerRemovedEventListener>();

    public DefaultGridServiceContainerRemovedEventManager(InternalGridServiceContainers gridServiceContainers) {
        this.gridServiceContainers = gridServiceContainers;
        this.admin = (InternalAdmin) gridServiceContainers.getAdmin();
    }

    public void gridServiceContainerRemoved(final GridServiceContainer gridServiceContainer) {
        for (final GridServiceContainerRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceContainerRemoved(gridServiceContainer);
                }
            });
        }
    }

    public void add(GridServiceContainerRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(GridServiceContainerRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceContainerRemovedEventListener(eventListener));
        } else {
            add((GridServiceContainerRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceContainerRemovedEventListener(eventListener));
        } else {
            remove((GridServiceContainerRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}