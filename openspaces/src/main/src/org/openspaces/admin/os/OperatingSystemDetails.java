package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemDetails {

    boolean isNA();

    String getUid();

    String getName();

    String getArch();

    String getVersion();

    int getAvailableProcessors();

    long getTotalSwapSpaceSizeInBytes();

    double getTotalSwapSpaceSizeInMB();

    double getTotalSwapSpaceSizeInGB();

    long getTotalPhysicalMemorySizeInBytes();
    
    double getTotalPhysicalMemorySizeInMB();

    double getTotalPhysicalMemorySizeInGB();
}
