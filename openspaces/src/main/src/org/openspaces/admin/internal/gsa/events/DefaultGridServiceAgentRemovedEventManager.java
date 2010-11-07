package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.support.GroovyHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultGridServiceAgentRemovedEventManager implements InternalGridServiceAgentRemovedEventManager {

    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalAdmin admin;

    private final List<GridServiceAgentRemovedEventListener> listeners = new CopyOnWriteArrayList<GridServiceAgentRemovedEventListener>();

    public DefaultGridServiceAgentRemovedEventManager(InternalGridServiceAgents gridServiceAgents) {
        this.gridServiceAgents = gridServiceAgents;
        this.admin = (InternalAdmin) gridServiceAgents.getAdmin();
    }

    public void gridServiceAgentRemoved(final GridServiceAgent gridServiceAgent) {
        for (final GridServiceAgentRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.gridServiceAgentRemoved(gridServiceAgent);
                }
            });
        }
    }

    public void add(GridServiceAgentRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(GridServiceAgentRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureGridServiceAgentRemovedEventListener(eventListener));
        } else {
            add((GridServiceAgentRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureGridServiceAgentRemovedEventListener(eventListener));
        } else {
            remove((GridServiceAgentRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}