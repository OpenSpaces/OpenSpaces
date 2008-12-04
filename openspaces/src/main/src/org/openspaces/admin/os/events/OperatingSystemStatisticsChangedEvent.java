package org.openspaces.admin.os.events;

import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystemStatistics;

/**
 * @author kimchy
 */
public class OperatingSystemStatisticsChangedEvent {

    private final OperatingSystem operatingSystem;

    private final OperatingSystemStatistics statistics;

    public OperatingSystemStatisticsChangedEvent(OperatingSystem operatingSystem, OperatingSystemStatistics statistics) {
        this.operatingSystem = operatingSystem;
        this.statistics = statistics;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public OperatingSystemStatistics getStatistics() {
        return statistics;
    }
}