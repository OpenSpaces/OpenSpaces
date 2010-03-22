package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;

/**
 * @author kimchy
 */
public class DefaultTransportStatistics implements TransportStatistics {

    private final static NIOStatistics NA_STATS = new NIOStatistics();

    private final long timeDelta;

    private final NIOStatistics stats;

    private volatile TransportStatistics previousStats;

    private final TransportDetails details;

    public DefaultTransportStatistics() {
        this(NA_STATS, null, null, 0, -1);
    }

    public DefaultTransportStatistics(NIOStatistics stats, TransportStatistics previousStats, TransportDetails details, int historySize, long timeDelta) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
        this.timeDelta = timeDelta;

        TransportStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultTransportStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public long getAdminTimestamp() {
        if (stats.getTimestamp() != -1 && timeDelta != Integer.MIN_VALUE) {
            return stats.getTimestamp() + timeDelta;
        }
        return -1;
    }
    
    public TransportDetails getDetails() {
        return this.details;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public TransportStatistics getPrevious() {
        return this.previousStats;
    }

    public void setPreviousStats(TransportStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public long getCompletedTaskCount() {
        return stats.getCompletedTaskCount();
    }

    public double getCompletedTaskPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getCompletedTaskCount(), getPrevious().getCompletedTaskCount(), getTimestamp(), getPreviousTimestamp());
    }

    public int getActiveThreadsCount() {
        return stats.getActiveThreadsCount();
    }

    public double getActiveThreadsPerc() {
        return StatisticsUtils.computePerc(getActiveThreadsCount(), getDetails().getMaxThreads());
    }

    public int getQueueSize() {
        return stats.getQueueSize();
    }
}
