package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportStatistics {

    boolean isNA();

    long getTimestamp();

    long getPreviousTimestamp();

    TransportDetails getDetails();

    TransportStatistics getPrevious();

    long getCompletedTaskCount();

    double getCompletedTaskPerSecond();

    int getActiveThreadsCount();

    double getActiveThreadsPerc();

    int getQueueSize();
}
