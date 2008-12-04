package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceStatisticsChangedEventManager {

    void add(SpaceStatisticsChangedEventListener eventListener);

    void remove(SpaceStatisticsChangedEventListener eventListener);
}