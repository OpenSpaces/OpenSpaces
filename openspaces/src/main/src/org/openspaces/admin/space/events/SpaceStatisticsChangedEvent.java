package org.openspaces.admin.space.events;

import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * @author kimchy
 */
public class SpaceStatisticsChangedEvent {

    private final Space space;

    private final SpaceStatistics statistics;

    public SpaceStatisticsChangedEvent(Space space, SpaceStatistics statistics) {
        this.space = space;
        this.statistics = statistics;
    }

    public Space getSpace() {
        return space;
    }

    public SpaceStatistics getStatistics() {
        return statistics;
    }
}