package org.openspaces.admin.internal.vm;

import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.VirtualMachinesDetails;
import org.openspaces.admin.vm.VirtualMachinesStatistics;

/**
 * @author kimchy
 */
public class DefaultVirtualMachinesStatistics implements VirtualMachinesStatistics {

    private final long timestamp;

    private final VirtualMachineStatistics[] virutualMachinesStatistics;

    private final VirtualMachinesDetails details;

    private volatile VirtualMachinesStatistics previousStats;

    public DefaultVirtualMachinesStatistics(VirtualMachineStatistics[] virutualMachinesStatistics, VirtualMachinesDetails details,
                                            VirtualMachinesStatistics previousStats, int historySize) {
        this.timestamp = System.currentTimeMillis();
        this.virutualMachinesStatistics = virutualMachinesStatistics;
        this.details = details;
        this.previousStats = previousStats;

        VirtualMachinesStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultVirtualMachinesStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return virutualMachinesStatistics == null || virutualMachinesStatistics[0].isNA();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public VirtualMachinesStatistics getPrevious() {
        return previousStats;
    }

    public void setPreviousStats(VirtualMachinesStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public int getSize() {
        return virutualMachinesStatistics.length;
    }

    public VirtualMachinesDetails getDetails() {
        return this.details;
    }

    public long getUptime() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getUptime();
        }
        return total;
    }

    public long getMemoryHeapCommittedInBytes() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getMemoryHeapCommittedInBytes();
            }
        }
        return total;
    }

    public double getMemoryHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapCommittedInBytes());
    }

    public double getMemoryHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapCommittedInBytes());
    }

    public long getMemoryHeapUsedInBytes() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getMemoryHeapUsedInBytes();
            }
        }
        return total;
    }

    public double getMemoryHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getDetails().getMemoryHeapMaxInBytes());
    }

    public double getMemoryHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getMemoryHeapCommittedInBytes());
    }

    public long getMemoryNonHeapCommittedInBytes() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getMemoryNonHeapCommittedInBytes();
            }
        }
        return total;
    }

    public double getMemoryNonHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapCommittedInBytes());
    }

    public double getMemoryNonHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapCommittedInBytes());
    }

    public long getMemoryNonHeapUsedInBytes() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getMemoryNonHeapUsedInBytes();
            }
        }
        return total;
    }

    public double getMemoryNonHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getDetails().getMemoryNonHeapMaxInBytes());
    }

    public double getMemoryNonHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getMemoryNonHeapCommittedInBytes());
    }

    public int getThreadCount() {
        int total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getThreadCount();
            }
        }
        return total;
    }

    public int getPeakThreadCount() {
        int total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getPeakThreadCount();
            }
        }
        return total;
    }

    public long getGcCollectionCount() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getGcCollectionCount();
            }
        }
        return total;
    }

    public long getGcCollectionTime() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            if (!stats.isNA()) {
                total += stats.getGcCollectionTime();
            }
        }
        return total;
    }

    public double getGcCollectionPerc() {
        double total = 0;
        int size = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            double perc = stats.getGcCollectionPerc();
            if (perc != -1) {
                total += perc;
                size++;
            }
        }
        if (size == 0) {
            return 0;
        }
        return total / size;
    }
}
