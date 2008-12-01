package org.openspaces.admin.lus.events;

import org.openspaces.admin.lus.LookupService;

/**
 * @author kimchy
 */
public interface LookupServiceAddedEventListener {

    void lookupServiceAdded(LookupService lookupService);
}