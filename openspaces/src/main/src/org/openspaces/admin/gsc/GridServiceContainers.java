package org.openspaces.admin.gsc;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface GridServiceContainers extends Iterable<GridServiceContainer> {

    Admin getAdmin();

    GridServiceContainer[] getContainers();

    GridServiceContainer getContainerByUID(String uid);

    Map<String, GridServiceContainer> getUids();

    int getSize();

    boolean isEmpty();

    GridServiceContainerAddedEventManager getGridServiceContainerAdded();

    GridServiceContainerRemovedEventManager getGridServiceContainerRemoved();
}