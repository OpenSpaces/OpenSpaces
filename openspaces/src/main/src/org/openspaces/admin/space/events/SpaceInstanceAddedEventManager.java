package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceInstanceAddedEventManager {

    void add(SpaceInstanceAddedEventListener eventListener);

    void remove(SpaceInstanceAddedEventListener eventListener);
}