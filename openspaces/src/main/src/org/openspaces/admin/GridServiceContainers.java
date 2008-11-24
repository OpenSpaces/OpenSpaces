package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface GridServiceContainers extends Iterable<GridServiceContainer> {

    GridServiceContainer[] getContainers();

    GridServiceContainer getContainerByUID(String uid);

    int size();
}