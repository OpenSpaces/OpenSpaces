package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceRemovedEventManager {

    void add(SpaceRemovedEventListener eventListener);

    void remove(SpaceRemovedEventListener eventListener);
}