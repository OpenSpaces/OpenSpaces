package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceStatisticsChangedEventListener {

    void spaceStatisticsChanged(SpaceStatisticsChangedEvent event);
}