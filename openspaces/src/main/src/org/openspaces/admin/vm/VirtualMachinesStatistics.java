package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatistics {

    boolean isNA();

    long getTimestamp();

    VirtualMachinesDetails getDetails();

    int getSize();

    long getUptime();

    long getMemoryHeapCommitted();

    long getMemoryHeapUsed();

    /**
     * Returns the memory heap percentage from used to the max.
     */
    double getMemoryHeapPerc();

    /**
     * Returns the memory heap percentage from used to committed.
     */
    double getMemoryHeapCommittedPerc();

    long getMemoryNonHeapCommitted();

    long getMemoryNonHeapUsed();

    /**
     * Returns the memory non heap percentage from used to the max.
     */
    double getMemoryNonHeapPerc();

    /**
     * Returns the memory non heap percentage from used to committed.
     */
    double getMemoryNonHeapCommittedPerc();

    int getThreadCount();

    int getPeakThreadCount();

    long getGcCollectionCount();

    long getGcCollectionTime();

    /**
     * The percentage of the gc collection time between the current sampled statistics
     * and the previous one.
     */
    double getGcCollectionPerc();
}