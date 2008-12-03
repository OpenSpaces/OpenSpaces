package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceInstanceRemovedEventManager {

    void add(SpaceInstanceRemovedEventListener eventListener);

    void remove(SpaceInstanceRemovedEventListener eventListener);
}