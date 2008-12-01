package org.openspaces.admin.internal.gsm.events;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author kimchy
 */
public class ClosureGridServiceManagerRemovedEventListener extends AbstractClosureEventListener implements GridServiceManagerRemovedEventListener {

    public ClosureGridServiceManagerRemovedEventListener(Object closure) {
        super(closure);
    }

    public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
        getClosure().call(gridServiceManager);
    }
}