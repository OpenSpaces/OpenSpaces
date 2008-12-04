package org.openspaces.admin.gsm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface GridServiceManagers extends AdminAware, Iterable<GridServiceManager> {

    GridServiceManager[] getManagers();

    GridServiceManager getManagerByUID(String uid);

    Map<String, GridServiceManager> getUids();

    int getSize();

    boolean isEmpty();

    /**
     * Waits till at least the provided number of GSMs are up.
     */
    boolean waitFor(int numberOfGridServiceManagers);

    /**
     * Waits till at least the provided number of GSMs are up for the specified timeout.
     */
    boolean waitFor(int numberOfGridServiceManagers, long timeout, TimeUnit timeUnit);

    void addLifecycleListener(GridServiceManagerLifecycleEventListener eventListener);

    void removeLifecycleListener(GridServiceManagerLifecycleEventListener eventListener);

    GridServiceManagerAddedEventManager getGridServiceManagerAdded();

    GridServiceManagerRemovedEventManager getGridServiceManagerRemoved();
}
