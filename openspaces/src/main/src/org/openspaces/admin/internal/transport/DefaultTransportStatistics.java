package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import org.openspaces.admin.transport.TransportStatistics;

/**
 * @author kimchy
 */
public class DefaultTransportStatistics implements TransportStatistics {

    private final static NIOStatistics NA_STATS = new NIOStatistics();

    private final NIOStatistics stats;

    public DefaultTransportStatistics() {
        this.stats = NA_STATS;
    }

    public DefaultTransportStatistics(NIOStatistics stats) {
        this.stats = stats;
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public long getCompletedTaskCount() {
        return stats.getCompletedTaskCount();
    }

    public int getActiveThreadsCount() {
        return stats.getActiveThreadsCount();
    }

    public int getQueueSize() {
        return stats.getQueueSize();
    }
}
