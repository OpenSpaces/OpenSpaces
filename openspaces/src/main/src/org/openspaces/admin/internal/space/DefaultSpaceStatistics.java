package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * @author kimchy
 */
public class DefaultSpaceStatistics implements SpaceStatistics {

    private final long timestamp;

    private final SpaceInstanceStatistics[] stats;

    public DefaultSpaceStatistics(SpaceInstanceStatistics[] stats) {
        this.stats = stats;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isNA() {
        return stats == null || stats[0].isNA();
    }

    public int getSize() {
        return stats.length;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getWriteCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getWriteCount();
        }
        return total;
    }

    public long getReadCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getReadCount();
        }
        return total;
    }

    public long getTakeCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getTakeCount();
        }
        return total;
    }

    public long getNotifyRegistrationCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyRegistrationCount();
        }
        return total;
    }

    public long getCleanCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyTriggerCount();
        }
        return total;
    }

    public long getUpdateCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getUpdateCount();
        }
        return total;
    }

    public long getNotifyTriggerCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyTriggerCount();
        }
        return total;
    }

    public long getNotifyAckCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyAckCount();
        }
        return total;
    }

    public long getExecuteCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getExecuteCount();
        }
        return total;
    }

    public long getRemoveCount() {
        long total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getRemoveCount();
        }
        return total;
    }
}
