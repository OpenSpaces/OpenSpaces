package org.openspaces.admin.space.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface SpaceStatisticsChangedEventListener extends AdminEventListener {

    void spaceStatisticsChanged(SpaceStatisticsChangedEvent event);
}