package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachineStatistics {

    boolean isNA();

    long getTimestamp();

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
