package org.openspaces.admin.internal.space;

import com.j_spaces.core.filters.StatisticsHolder;
import org.openspaces.admin.space.SpaceInstanceStatistics;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceStatistics implements SpaceInstanceStatistics {

    private final StatisticsHolder statisticsHolder;

    public DefaultSpaceInstanceStatistics(StatisticsHolder statisticsHolder) {
        this.statisticsHolder = statisticsHolder;
    }

    public boolean isNA() {
        return statisticsHolder.getOperationsCount()[0] == -1;
    }

    public long getTimestamp() {
        return statisticsHolder.getTimestamp();
    }

    public long getWriteCount() {
        return statisticsHolder.getOperationsCount()[0];
    }

    public long getReadCount() {
        return statisticsHolder.getOperationsCount()[1];
    }

    public long getTakeCount() {
        return statisticsHolder.getOperationsCount()[2];
    }

    public long getNotifyRegistrationCount() {
        return statisticsHolder.getOperationsCount()[3];
    }

    public long getCleanCount() {
        return statisticsHolder.getOperationsCount()[4];
    }

    public long getUpdateCount() {
        return statisticsHolder.getOperationsCount()[5];
    }

    public long getReadMultipleCount() {
        return statisticsHolder.getOperationsCount()[6];
    }

    public long getTakeMultipleCount() {
        return statisticsHolder.getOperationsCount()[7];
    }

    public long getNotifyTriggerCount() {
        return statisticsHolder.getOperationsCount()[8];
    }

    public long getNotifyAckCount() {
        return statisticsHolder.getOperationsCount()[9];
    }

    public long getExecuteCount() {
        return statisticsHolder.getOperationsCount()[10];
    }

    /**
     * Remove happens when an entry is removed due to lease expiration or lease cancel.
     */
    public long getRemoveCount() {
        return statisticsHolder.getOperationsCount()[11];
    }
}