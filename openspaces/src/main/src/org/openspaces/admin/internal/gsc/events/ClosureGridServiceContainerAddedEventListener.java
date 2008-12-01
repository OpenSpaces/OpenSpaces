package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceContainerAddedEventListener extends AbstractClosureEventListener implements GridServiceContainerAddedEventListener {

    public ClosureGridServiceContainerAddedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        getClosure().call(gridServiceContainer);
    }
}