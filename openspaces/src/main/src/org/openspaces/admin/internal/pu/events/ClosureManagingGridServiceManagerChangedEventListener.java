package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureManagingGridServiceManagerChangedEventListener extends AbstractClosureEventListener implements ManagingGridServiceManagerChangedEventListener {

    public ClosureManagingGridServiceManagerChangedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
        getClosure().call(event);
    }
}