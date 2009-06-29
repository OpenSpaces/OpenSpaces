package org.openspaces.admin.internal.os;

import com.gigaspaces.internal.os.OSDetails;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.support.StatisticsUtils;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemDetails implements OperatingSystemDetails {

    private final OSDetails details;

    public DefaultOperatingSystemDetails(OSDetails details) {
        this.details = details;
    }

    public boolean isNA() {
        return details.isNA();
    }

    public String getUid() {
        return details.getUID();
    }

    public String getName() {
        return details.getName();
    }

    public String getArch() {
        return details.getArch();
    }

    public String getVersion() {
        return details.getVersion();
    }

    public int getAvailableProcessors() {
        return details.getAvailableProcessors();
    }

    public long getTotalSwapSpaceSizeInBytes() {
        return details.getTotalSwapSpaceSize();
    }

    public double getTotalSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getTotalSwapSpaceSizeInBytes());
    }

    public double getTotalSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getTotalSwapSpaceSizeInBytes());
    }

    public long getTotalPhysicalMemorySizeInBytes() {
        return details.getTotalPhysicalMemorySize();
    }

    public double getTotalPhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getTotalPhysicalMemorySizeInBytes());
    }

    public double getTotalPhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getTotalPhysicalMemorySizeInBytes());
    }

    public String getHostName() {
        return details.getHostName();
    }

    public String getHostAddress() {
        return details.getHostAddress();
    }
}
