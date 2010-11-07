package org.openspaces.admin.internal.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagerRemovedEventManager implements InternalGridServiceManagerRemovedEventManager {

    private final InternalGridServiceManagers gridServiceManagers;

    private final InternalAdmin admin;

    private final List<GridServiceManagerRemovedEventListener> listeners = new CopyOnWriteArrayList<GridServiceManagerRemovedEventListener>();

    public DefaultGridServiceManagerRemovedEventManager(InternalGridServiceManagers gridServiceManagers) {
        this.gridServiceManagers = gridServiceManagers;
        this.admin = (InternalAdmin) gridServiceManagers.getAdmin();
    }

    public void gridServiceManagerRemoved(final GridServiceManager GridServiceManager) {
        for (final GridServiceManagerRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceManagerRemoved(GridServiceManager);
                }
            });
        }
    }

    public void add(GridServiceManagerRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(GridServiceManagerRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceManagerRemovedEventListener(eventListener));
        } else {
            add((GridServiceManagerRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceManagerRemovedEventListener(eventListener));
        } else {
            remove((GridServiceManagerRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}