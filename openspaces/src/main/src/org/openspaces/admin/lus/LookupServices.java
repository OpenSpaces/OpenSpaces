package org.openspaces.admin.lus;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.lus.events.LookupServiceAddedEventManager;
import org.openspaces.admin.lus.events.LookupServiceLifecycleEventListener;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface LookupServices extends AdminAware, Iterable<LookupService> {

    LookupService[] getLookupServices();

    LookupService getLookupServiceByUID(String id);

    Map<String, LookupService> getUids();

    int size();

    boolean isEmpty();

    /**
     * Waits till at least the provided number of Lookup Services are up.
     */
    boolean waitFor(int numberOfLookupServices);

    /**
     * Waits till at least the provided number of Lookup Services are up for the specified timeout.
     */
    boolean waitFor(int numberOfLookupServices, long timeout, TimeUnit timeUnit);

    void addLifecycleListener(LookupServiceLifecycleEventListener eventListener);

    void removeLifecycleListener(LookupServiceLifecycleEventListener eventListener);

    LookupServiceAddedEventManager getLookupServiceAdded();

    LookupServiceRemovedEventManager getLookupServiceRemoved();
}
