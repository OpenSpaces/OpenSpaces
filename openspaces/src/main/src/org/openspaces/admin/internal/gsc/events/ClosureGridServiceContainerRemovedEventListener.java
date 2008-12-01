package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceContainerRemovedEventListener extends AbstractClosureEventListener implements GridServiceContainerRemovedEventListener {

    public ClosureGridServiceContainerRemovedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        getClosure().call(gridServiceContainer);
    }
}