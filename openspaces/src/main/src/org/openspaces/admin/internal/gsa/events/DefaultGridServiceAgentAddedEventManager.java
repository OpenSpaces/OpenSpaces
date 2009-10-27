package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgentAddedEventManager implements InternalGridServiceAgentAddedEventManager {

    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalAdmin admin;

    private final List<GridServiceAgentAddedEventListener> listeners = new CopyOnWriteArrayList<GridServiceAgentAddedEventListener>();

    public DefaultGridServiceAgentAddedEventManager(InternalGridServiceAgents gridServiceAgents) {
        this.gridServiceAgents = gridServiceAgents;
        this.admin = (InternalAdmin) gridServiceAgents.getAdmin();
    }

    public void gridServiceAgentAdded(final GridServiceAgent gridServiceAgent) {
        for (final GridServiceAgentAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceAgentAdded(gridServiceAgent);
                }
            });
        }
    }

    public void add(final GridServiceAgentAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (GridServiceAgent gridServiceAgent : gridServiceAgents) {
                        eventListener.gridServiceAgentAdded(gridServiceAgent);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void add(final GridServiceAgentAddedEventListener eventListener) {
        add(eventListener, true);
    }

    public void remove(GridServiceAgentAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceAgentAddedEventListener(eventListener));
        } else {
            add((GridServiceAgentAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceAgentAddedEventListener(eventListener));
        } else {
            remove((GridServiceAgentAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}