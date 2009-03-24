package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpaceStatistics;

/**
 * @author kimchy
 */
public class DefaultSpaceStatistics implements SpaceStatistics {

    private final long timestamp;

    private final SpaceInstanceStatistics[] stats;

    private volatile SpaceStatistics previos;

    public DefaultSpaceStatistics(SpaceInstanceStatistics[] stats, SpaceStatistics previos, int historySize) {
        this.stats = stats;
        this.timestamp = System.currentTimeMillis();
        this.previos = previos;

        SpaceStatistics lastStats = null;
        for (int i = 0; i < historySize; i++) {
            if (getPrevious() == null) {
                lastStats = null;
                break;
            }
            lastStats = getPrevious();
        }
        if (lastStats != null) {
            ((DefaultSpaceStatistics) lastStats).setPrevios(null);
        }
    }

    public boolean isNA() {
        return stats == null || stats[0].isNA();
    }

    public SpaceStatistics getPrevious() {
        return previos;
    }

    public void setPrevios(SpaceStatistics previos) {
        this.previos = previos;
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

    public double getWritePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getWritePerSecond();
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

    public double getReadPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getReadPerSecond();
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

    public double getTakePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getTakePerSecond();
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

    public double getNotifyRegistrationPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyRegistrationPerSecond();
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

    public double getCleanPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getCleanPerSecond();
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

    public double getUpdatePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getUpdatePerSecond();
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

    public double getNotifyTriggerPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyRegistrationPerSecond();
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

    public double getNotifyAckPerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getNotifyAckPerSecond();
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

    public double getExecutePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getExecutePerSecond();
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

    public double getRemovePerSecond() {
        double total = 0;
        for (SpaceInstanceStatistics stat : stats) {
            total += stat.getRemovePerSecond();
        }
        return total;
    }
}
