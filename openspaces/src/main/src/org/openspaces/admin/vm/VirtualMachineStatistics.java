package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachineStatistics {

    /**
     * Returns <code>true</code> if this is not valid statistics.
     */
    boolean isNA();

    /**
     * Returns the details of the virtual machine.
     */
    VirtualMachineDetails getDetails();

    /**
     * Returns the previous statistics sampled. <code>null</code> if this is the first one.
     */
    VirtualMachineStatistics getPrevious();

    /**
     * Returns the previous timestamp of the statistics sampled, <code>-1</code> if this is the
     * first one.
     */
    long getPreviousTimestamp();

    long getTimestamp();

    long getUptime();

    long getMemoryHeapCommittedInBytes();
    double getMemoryHeapCommittedInMB();
    double getMemoryHeapCommittedInGB();

    long getMemoryHeapUsedInBytes();
    double getMemoryHeapUsedInMB();
    double getMemoryHeapUsedInGB();

    /**
     * Returns the memory heap percentage from used to the max.
     */
    double getMemoryHeapPerc();

    /**
     * Returns the memory heap percentage from used to committed.
     */
    double getMemoryHeapCommittedPerc();

    long getMemoryNonHeapCommittedInBytes();
    double getMemoryNonHeapCommittedInMB();
    double getMemoryNonHeapCommittedInGB();

    long getMemoryNonHeapUsedInBytes();
    double getMemoryNonHeapUsedInMB();
    double getMemoryNonHeapUsedInGB();

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
     * and the previous one. If there is no previous one, will reutrn -1.
     */
    double getGcCollectionPerc();
}
