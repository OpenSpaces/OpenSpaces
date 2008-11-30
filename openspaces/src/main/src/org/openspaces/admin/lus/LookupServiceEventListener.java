package org.openspaces.admin.lus;

/**
 * @author kimchy
 */
public interface LookupServiceEventListener {

    void lookupServiceAdded(LookupService lookupService);

    void lookupServiceRemoved(LookupService lookupService);
}
