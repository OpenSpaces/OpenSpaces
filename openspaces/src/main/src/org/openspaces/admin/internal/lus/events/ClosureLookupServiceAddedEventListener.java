package org.openspaces.admin.internal.lus.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureLookupServiceAddedEventListener extends AbstractClosureEventListener implements LookupServiceAddedEventListener {

    public ClosureLookupServiceAddedEventListener(Object closure) {
        super(closure);
    }

    public void lookupServiceAdded(LookupService lookupService) {
        getClosure().call(lookupService);
    }
}