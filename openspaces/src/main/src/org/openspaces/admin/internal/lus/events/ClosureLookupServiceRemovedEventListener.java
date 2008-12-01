package org.openspaces.admin.internal.lus.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureLookupServiceRemovedEventListener extends AbstractClosureEventListener implements LookupServiceRemovedEventListener {

    public ClosureLookupServiceRemovedEventListener(Object closure) {
        super(closure);
    }

    public void lookupServiceRemoved(LookupService lookupService) {
        getClosure().call(lookupService);
    }
}