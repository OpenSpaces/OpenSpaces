package org.openspaces.admin.internal.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceManagerAddedEventListener extends AbstractClosureEventListener implements GridServiceManagerAddedEventListener {

    public ClosureGridServiceManagerAddedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
        getClosure().call(gridServiceManager);
    }
}