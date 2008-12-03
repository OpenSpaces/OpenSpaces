package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceModeChangedEventManager {

    void add(SpaceModeChangedEventListener eventListener);

    void remove(SpaceModeChangedEventListener eventListener);
}