package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemsDetails;
import org.openspaces.admin.support.StatisticsUtils;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsDetails implements OperatingSystemsDetails {

    private final OperatingSystemDetails[] details;

    public DefaultOperatingSystemsDetails(OperatingSystemDetails[] details) {
        this.details = details;
    }

    public int getAvailableProcessors() {
        int total = 0;
        for (OperatingSystemDetails detail : details) {
            total += detail.getAvailableProcessors();
        }
        return total;
    }

    public long getTotalSwapSpaceSizeInBytes() {
        long total = 0;
        for (OperatingSystemDetails detail : details) {
            if (detail.getTotalSwapSpaceSizeInBytes() != -1) {
                total += detail.getTotalSwapSpaceSizeInBytes();
            }
        }
        return total;
    }

    public double getTotalSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getTotalSwapSpaceSizeInBytes());
    }

    public double getTotalSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getTotalSwapSpaceSizeInBytes());
    }

    public long getTotalPhysicalMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemDetails detail : details) {
            if (detail.getTotalPhysicalMemorySizeInBytes() != -1) {
                total += detail.getTotalPhysicalMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getTotalPhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getTotalPhysicalMemorySizeInBytes());
    }

    public double getTotalPhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getTotalPhysicalMemorySizeInBytes());
    }
}
