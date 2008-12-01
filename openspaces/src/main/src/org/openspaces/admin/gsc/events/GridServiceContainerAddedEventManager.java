package org.openspaces.admin.gsc.events;

/**
 * @author kimchy
 */
public interface GridServiceContainerAddedEventManager {

    void add(GridServiceContainerAddedEventListener eventListener);

    void remove(GridServiceContainerAddedEventListener eventListener);

}