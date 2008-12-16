package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemStatistics {

    boolean isNA();

    long getTimestamp();

    OperatingSystemDetails getDetails();

    OperatingSystemStatistics getPrevious();

    long getCommittedVirtualMemorySizeInBytes();

    double getCommittedVirtualMemorySizeInMB();

    double getCommittedVirtualMemorySizeInGB();

    long getFreeSwapSpaceSizeInBytes();

    double getFreeSwapSpaceSizeInMB();

    double getFreeSwapSpaceSizeInGB();

    long getFreePhysicalMemorySizeInBytes();

    double getFreePhysicalMemorySizeInMB();

    double getFreePhysicalMemorySizeInGB();

    double getSystemLoadAverage();
}
