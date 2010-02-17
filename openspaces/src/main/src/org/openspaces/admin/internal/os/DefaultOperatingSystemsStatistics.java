package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemStatistics;
import org.openspaces.admin.os.OperatingSystemsDetails;
import org.openspaces.admin.os.OperatingSystemsStatistics;
import org.openspaces.admin.support.StatisticsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsStatistics implements OperatingSystemsStatistics {

    private final long timestamp;

    private final OperatingSystemStatistics[] stats;

    private volatile OperatingSystemsStatistics previousStats;

    private final OperatingSystemsDetails details;

    public DefaultOperatingSystemsStatistics(OperatingSystemStatistics[] stats, OperatingSystemsStatistics previousStats, OperatingSystemsDetails details,
                                             int historySize) {
        this.timestamp = System.currentTimeMillis();
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;

        OperatingSystemsStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultOperatingSystemsStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats == null || stats.length == 0 || stats[0].isNA();
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getSize() {
        return stats.length;
    }

    public OperatingSystemsStatistics getPrevious() {
        return this.previousStats;
    }

    public List<OperatingSystemsStatistics> getTimeline() {
        ArrayList<OperatingSystemsStatistics> timeline = new ArrayList<OperatingSystemsStatistics>();
        timeline.add(this);
        OperatingSystemsStatistics current = this.getPrevious();
        while (current != null && !current.isNA()) {
            timeline.add(current);
            current = current.getPrevious();
        }
        return timeline;
    }

    public void setPreviousStats(OperatingSystemsStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public OperatingSystemsDetails getDetails() {
        return this.details;
    }

    public long getFreeSwapSpaceSizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getFreeSwapSpaceSizeInBytes() != -1) {
                total += stat.getFreeSwapSpaceSizeInBytes();
            }
        }
        return total;
    }

    public double getFreeSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getFreeSwapSpaceSizeInBytes());
    }

    public double getFreeSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getFreeSwapSpaceSizeInBytes());
    }

    public long getFreePhysicalMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getFreePhysicalMemorySizeInBytes() != -1) {
                total += stat.getFreePhysicalMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getFreePhysicalMemorySizeInBytes());
    }

    public double getFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getFreePhysicalMemorySizeInBytes());
    }

    public long getActualFreePhysicalMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getActualFreePhysicalMemorySizeInBytes() != -1) {
                total += stat.getActualFreePhysicalMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getActualFreePhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getActualFreePhysicalMemorySizeInBytes());
    }

    public double getActualFreePhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getActualFreePhysicalMemorySizeInBytes());
    }

    public double getCpuPerc() {
        int count = 0;
        double total = 0;
        for (OperatingSystemStatistics stat : stats) {
            if (stat.getCpuPerc() != -1) {
                count++;
                total += stat.getCpuPerc();
            }
        }
        if (count == 0) {
            return -1;
        }
        return total / count;
    }

    public String getCpuPercFormatted() {
        return StatisticsUtils.formatPerc(getCpuPerc());
    }
}
