package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemsDetails {

    int getAvailableProcessors();

    long getTotalSwapSpaceSizeInBytes();

    double getTotalSwapSpaceSizeInMB();

    double getTotalSwapSpaceSizeInGB();

    long getTotalPhysicalMemorySizeInBytes();

    double getTotalPhysicalMemorySizeInMB();

    double getTotalPhysicalMemorySizeInGB();
}
