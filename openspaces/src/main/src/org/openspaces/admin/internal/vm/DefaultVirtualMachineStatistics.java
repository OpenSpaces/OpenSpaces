package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMStatistics;
import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatistics implements VirtualMachineStatistics {

    private static final JVMStatistics NA_STATS = new JVMStatistics();

    private final JVMStatistics stats;

    private final VirtualMachineDetails details;

    private final VirtualMachineStatistics previousStats;

    public DefaultVirtualMachineStatistics() {
        this(NA_STATS, null, null);
    }

    public DefaultVirtualMachineStatistics(JVMStatistics stats, VirtualMachineStatistics previousStats, VirtualMachineDetails details) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    public VirtualMachineStatistics getPrevious() {
        return previousStats;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public long getUptime() {
        return stats.getUptime();
    }

    public long getMemoryHeapCommitted() {
        return stats.getMemoryHeapCommitted();
    }

    public long getMemoryHeapUsed() {
        return stats.getMemoryHeapUsed();
    }

    public double getMemoryHeapPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerc(getMemoryHeapUsed(), getDetails().getMemoryHeapMax());
    }

    public double getMemoryHeapCommittedPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerc(getMemoryHeapUsed(), getMemoryHeapCommitted());
    }

    public long getMemoryNonHeapCommitted() {
        return stats.getMemoryNonHeapCommitted();
    }

    public long getMemoryNonHeapUsed() {
        return stats.getMemoryNonHeapUsed();
    }

    public double getMemoryNonHeapPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerc(getMemoryNonHeapUsed(), getDetails().getMemoryNonHeapMax());
    }

    public double getMemoryNonHeapCommittedPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerc(getMemoryNonHeapUsed(), getMemoryNonHeapCommitted());
    }

    public int getThreadCount() {
        return stats.getThreadCount();
    }

    public int getPeakThreadCount() {
        return stats.getPeakThreadCount();
    }

    public long getGcCollectionCount() {
        return stats.getGcCollectionCount();
    }

    public long getGcCollectionTime() {
        return stats.getGcCollectionTime();
    }

    public double getGcCollectionPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePercByTime(getGcCollectionTime(), previousStats.getGcCollectionTime(), getTimestamp(), getPreviousTimestamp());
    }
}
