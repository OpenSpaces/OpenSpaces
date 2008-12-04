package org.openspaces.admin.gsc;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface GridServiceContainers extends AdminAware, Iterable<GridServiceContainer> {

    GridServiceContainer[] getContainers();

    GridServiceContainer getContainerByUID(String uid);

    Map<String, GridServiceContainer> getUids();

    int getSize();

    boolean isEmpty();

    /**
     * Waits till at least the provided number of GSCs are up.
     */
    boolean waitFor(int numberOfGridServiceContainers);

    /**
     * Waits till at least the provided number of GSCs are up for the specified timeout.
     */
    boolean waitFor(int numberOfGridServiceContainers, long timeout, TimeUnit timeUnit);

    void addLifecycleListener(GridServiceContainerLifecycleEventListener eventListener);

    void removeLifecycleListener(GridServiceContainerLifecycleEventListener eventListener);

    GridServiceContainerAddedEventManager getGridServiceContainerAdded();

    GridServiceContainerRemovedEventManager getGridServiceContainerRemoved();
}