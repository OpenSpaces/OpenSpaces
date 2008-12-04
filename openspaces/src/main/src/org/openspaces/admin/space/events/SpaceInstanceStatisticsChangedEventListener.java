package org.openspaces.admin.space.events;

/**
 * @author kimchy
 */
public interface SpaceInstanceStatisticsChangedEventListener {

    void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event);
}