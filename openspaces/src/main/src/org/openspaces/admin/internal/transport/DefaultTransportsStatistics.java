package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.admin.transport.TransportsStatistics;

/**
 * @author kimchy
 */
public class DefaultTransportsStatistics implements TransportsStatistics {

    private final long timestamp;

    private final TransportStatistics[] stats;

    public DefaultTransportsStatistics(TransportStatistics[] stats) {
        this.stats = stats;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isNA() {
        return stats == null || stats.length == 0 || stats[0].isNA();
    }

    public int getSize() {
        return stats.length;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getCompletedTaskCount() {
        long total = 0;
        for (TransportStatistics stat : stats) {
            total += stat.getCompletedTaskCount();
        }
        return total;
    }

    public int getActiveThreadsCount() {
        int total = 0;
        for (TransportStatistics stat : stats) {
            total += stat.getActiveThreadsCount();
        }
        return total;
    }

    public int getQueueSize() {
        int total = 0;
        for (TransportStatistics stat : stats) {
            total += stat.getQueueSize();
        }
        return total;
    }
}
