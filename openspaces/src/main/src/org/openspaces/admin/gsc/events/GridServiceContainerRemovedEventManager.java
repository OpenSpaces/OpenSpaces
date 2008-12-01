package org.openspaces.admin.gsc.events;

/**
 * @author kimchy
 */
public interface GridServiceContainerRemovedEventManager {

    void add(GridServiceContainerRemovedEventListener eventListener);

    void remove(GridServiceContainerRemovedEventListener eventListener);

}