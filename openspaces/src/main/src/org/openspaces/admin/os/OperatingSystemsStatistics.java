package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemsStatistics {

    boolean isNA();

    long getTimestamp();

    int getSize();

    OperatingSystemsStatistics getPrevious();

    OperatingSystemsDetails getDetails();


    long getCommittedVirtualMemorySizeInBytes();

    double getCommittedVirtualMemorySizeInMB();

    double getCommittedVirtualMemorySizeInGB();

    long getFreeSwapSpaceSizeInBytes();

    double getFreeSwapSpaceSizeInMB();

    double getFreeSwapSpaceSizeInGB();

    long getFreePhysicalMemorySizeInBytes();

    double getFreePhysicalMemorySizeInMB();

    double getFreePhysicalMemorySizeInGB();
    

    double getTotalSystemLoadAverage();

    double getSystemLoadAverage();
}