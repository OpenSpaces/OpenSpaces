package org.openspaces.admin.lus;

import org.openspaces.admin.Admin;
import org.openspaces.admin.lus.events.LookupServiceAddedEventManager;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface LookupServices extends Iterable<LookupService> {

    Admin getAdmin();

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    Map<String, LookupService> getUids();

    int size();

    boolean isEmpty();

    LookupServiceAddedEventManager getLookupServiceAdded();

    LookupServiceRemovedEventManager getLookupServiceRemoved();
}
