package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportsStatistics {

    boolean isNA();

    int getSize();

    long getTimestamp();

    long getPreviousTimestamp();

    TransportsStatistics getPrevious();

    TransportsDetails getDetails();

    long getCompletedTaskCount();

    /**
     * Returns the aggergation of the each transport {@link org.openspaces.admin.transport.TransportStatistics#getCompletedTaskPerSecond()}.
     */
    double getCompletedTaskPerSecond();

    /**
     * Returns the aggregation of all the transports active thread counts.
     */
    int getActiveThreadsCount();

    /**
     * Returns the average percentage of all active thread count compared with the maximum
     * thread count.
     */
    double getActiveThreadsPerc();

    int getQueueSize();
}