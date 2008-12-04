package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportsStatistics {

    boolean isNA();

    int getSize();

    long getTimestamp();

    long getCompletedTaskCount();

    int getActiveThreadsCount();

    int getQueueSize();
}