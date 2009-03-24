package org.openspaces.admin.internal.os;

import com.gigaspaces.operatingsystem.OSStatistics;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.support.StatisticsUtils;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemStatistics implements OperatingSystemStatistics {

    private static final OSStatistics NA_STATS = new OSStatistics();

    private final OSStatistics stats;

    private final OperatingSystemDetails details;

    private volatile OperatingSystemStatistics previosStats;

    public DefaultOperatingSystemStatistics() {
        this(NA_STATS, null, null, 0);
    }

    public DefaultOperatingSystemStatistics(OSStatistics stats, OperatingSystemDetails details, OperatingSystemStatistics previosStats,
                                            int historySize) {
        this.stats = stats;
        this.details = details;
        this.previosStats = previosStats;
        OperatingSystemStatistics lastStats = null;
        for (int i = 0; i < historySize; i++) {
            if (getPrevious() == null) {
                lastStats = null;
                break;
            }
            lastStats = getPrevious();
        }
        if (lastStats != null) {
            ((DefaultOperatingSystemStatistics) lastStats).setPreviosStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public OperatingSystemDetails getDetails() {
        return this.details;
    }

    public OperatingSystemStatistics getPrevious() {
        return this.previosStats;
    }

    public void setPreviosStats(OperatingSystemStatistics previosStats) {
        this.previosStats = previosStats;
    }

    public long getFreeSwapSpaceSizeInBytes() {
        return stats.getFreeSwapSpaceSize();
    }

    public double getFreeSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getFreeSwapSpaceSizeInBytes());
    }

    public double getFreeSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getFreeSwapSpaceSizeInBytes());
    }

    public long getFreePhysicalMemorySizeInBytes() {
        return stats.getFreePhysicalMemorySize();
    }

    public double getFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getFreePhysicalMemorySizeInBytes());
    }

    public double getFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getFreePhysicalMemorySizeInBytes());
    }

    public double getSystemLoadAverage() {
        return stats.getSystemLoadAverage();
    }

    public double getCpuPerc() {
        return stats.getCpuPerc();
    }

    public long getOpenFilesCur() {
        return stats.getOpenFilesCur();
    }

    public String getCpuPercFormatted() {
        return StatisticsUtils.formatPerc(getCpuPerc());
    }
}
