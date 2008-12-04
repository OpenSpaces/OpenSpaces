package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatistics {

    boolean isNA();

    long getTimestamp();

    int getSize();

    long getUptime();

    long getMemoryHeapCommitted();

    long getMemoryHeapUsed();

    long getMemoryNonHeapCommitted();

    long getMemoryNonHeapUsed();

    int getThreadCount();

    int getPeakThreadCount();

    long getGcCollectionCount();

    long getGcCollectionTime();
}