package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceAgentRemovedEventListener extends AbstractClosureEventListener implements GridServiceAgentRemovedEventListener {

    public ClosureGridServiceAgentRemovedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        getClosure().call(gridServiceAgent);
    }
}