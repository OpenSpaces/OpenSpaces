package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceAddedEventManager {

    void add(SpaceAddedEventListener eventListener);

    void remove(SpaceAddedEventListener eventListener);
}