package org.openspaces.admin.space.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface SpaceInstanceStatisticsChangedEventListener extends AdminEventListener {

    void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event);
}