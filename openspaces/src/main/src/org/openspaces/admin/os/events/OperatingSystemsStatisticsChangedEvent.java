package org.openspaces.admin.os.events;

import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.os.OperatingSystemsStatistics;

/**
 * @author kimchy
 */
public class OperatingSystemsStatisticsChangedEvent {

    private final OperatingSystems operatingSystems;

    private final OperatingSystemsStatistics statistics;

    public OperatingSystemsStatisticsChangedEvent(OperatingSystems operatingSystems, OperatingSystemsStatistics statistics) {
        this.operatingSystems = operatingSystems;
        this.statistics = statistics;
    }

    public OperatingSystems getOperatingSystems() {
        return operatingSystems;
    }

    public OperatingSystemsStatistics getStatistics() {
        return statistics;
    }
}