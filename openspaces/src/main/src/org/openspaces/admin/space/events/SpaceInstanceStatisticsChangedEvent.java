package org.openspaces.admin.space.events;

import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceStatistics;

/**
 * @author kimchy
 */
public class SpaceInstanceStatisticsChangedEvent {

    private final SpaceInstance spaceInstance;

    private final SpaceInstanceStatistics statistics;

    public SpaceInstanceStatisticsChangedEvent(SpaceInstance spaceInstance, SpaceInstanceStatistics statistics) {
        this.spaceInstance = spaceInstance;
        this.statistics = statistics;
    }

    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    public SpaceInstanceStatistics getStatistics() {
        return statistics;
    }
}