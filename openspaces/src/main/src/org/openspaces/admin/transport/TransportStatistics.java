package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportStatistics {

    boolean isNA();

    long getTimestamp();

    long getCompletedTaskCount();

    int getActiveThreadsCount();

    int getQueueSize();
}
