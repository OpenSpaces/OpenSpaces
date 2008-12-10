package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceAgentAddedEventListener extends AbstractClosureEventListener implements GridServiceAgentAddedEventListener {

    public ClosureGridServiceAgentAddedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        getClosure().call(gridServiceAgent);
    }
}