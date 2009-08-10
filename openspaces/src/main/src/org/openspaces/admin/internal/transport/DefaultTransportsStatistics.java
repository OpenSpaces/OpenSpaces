package org.openspaces.admin.internal.transport;

import org.openspaces.admin.transport.TransportStatistics;
import org.openspaces.admin.transport.TransportsDetails;
import org.openspaces.admin.transport.TransportsStatistics;

/**
 * @author kimchy
 */
public class DefaultTransportsStatistics implements TransportsStatistics {

    private final long timestamp;

    private final TransportStatistics[] stats;

    private volatile TransportsStatistics previousStats;

    private final TransportsDetails details;

    public DefaultTransportsStatistics(TransportStatistics[] stats, TransportsStatistics previousStats, TransportsDetails details, int historySize) {
        this.stats = stats;
        this.timestamp = System.currentTimeMillis();
        this.previousStats = previousStats;
        this.details = details;

        TransportsStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultTransportsStatistics) lastStats).setPreviousStats(null);
        }
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

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public TransportsStatistics getPrevious() {
        return this.previousStats;
    }

    public void setPreviousStats(TransportsStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public TransportsDetails getDetails() {
        return this.details;
    }

    public long getCompletedTaskCount() {
        long total = 0;
        for (TransportStatistics stat : stats) {
            total += stat.getCompletedTaskCount();
        }
        return total;
    }

    public double getCompletedTaskPerSecond() {
        double total = 0;
        for (TransportStatistics stat : stats) {
            double completedTaskPerSecond = stat.getCompletedTaskPerSecond();
            if (completedTaskPerSecond != -1) {
                total += stat.getCompletedTaskPerSecond();
            }
        }
        return total;
    }

    public int getActiveThreadsCount() {
        int total = 0;
        for (TransportStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getActiveThreadsCount();
            }
        }
        return total;
    }

    public double getActiveThreadsPerc() {
        double total = 0;
        int size = 0;
        for (TransportStatistics stat : stats) {
            double perc = stat.getActiveThreadsPerc();
            if (perc != -1) {
                total += perc;
                size++;
            }
        }
        if (size == 0) {
            return 0;
        }
        return total / size;
    }

    public int getQueueSize() {
        int total = 0;
        for (TransportStatistics stat : stats) {
            if (!stat.isNA()) {
                total += stat.getQueueSize();
            }
        }
        return total;
    }
}
