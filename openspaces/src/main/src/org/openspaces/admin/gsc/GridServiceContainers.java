package org.openspaces.admin.gsc;

import java.util.Map;

/**
 * @author kimchy
 */
public interface GridServiceContainers extends Iterable<GridServiceContainer> {

    GridServiceContainer[] getContainers();

    GridServiceContainer getContainerByUID(String uid);

    Map<String, GridServiceContainer> getUids();

    int getSize();

    boolean isEmpty();

    void addEventListener(GridServiceContainerEventListener eventListener);

    void removeEventListener(GridServiceContainerEventListener eventListener);
}