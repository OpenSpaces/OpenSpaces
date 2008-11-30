package org.openspaces.admin.gsc;

/**
 * @author kimchy
 */
public interface GridServiceContainers extends Iterable<GridServiceContainer> {

    GridServiceContainer[] getContainers();

    GridServiceContainer getContainerByUID(String uid);

    int size();

    boolean isEmpty();

    void addEventListener(GridServiceContainerEventListener eventListener);

    void removeEventListener(GridServiceContainerEventListener eventListener);
}