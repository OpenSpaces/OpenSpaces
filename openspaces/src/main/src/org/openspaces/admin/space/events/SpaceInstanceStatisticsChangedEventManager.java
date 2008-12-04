package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceInstanceStatisticsChangedEventManager {

    void add(SpaceInstanceStatisticsChangedEventListener eventListener);

    void remove(SpaceInstanceStatisticsChangedEventListener eventListener);
}