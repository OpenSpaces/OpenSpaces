package org.openspaces.admin.internal.vm;

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

    private final VirtualMachinesStatistics previousStats;

    public DefaultVirtualMachinesStatistics(VirtualMachineStatistics[] virutualMachinesStatistics, VirtualMachinesDetails details,
                                            VirtualMachinesStatistics previousStats) {
        this.timestamp = System.currentTimeMillis();
        this.virutualMachinesStatistics = virutualMachinesStatistics;
        this.details = details;
        this.previousStats = previousStats;
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

    public long getMemoryHeapCommitted() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getMemoryHeapCommitted();
        }
        return total;
    }

    public long getMemoryHeapUsed() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getMemoryHeapUsed();
        }
        return total;
    }

    public double getMemoryHeapPerc() {
        return ((double) getMemoryHeapUsed()) / getDetails().getMemoryHeapMax() * 100;
    }

    public double getMemoryHeapCommittedPerc() {
        return ((double) getMemoryHeapUsed()) / getMemoryHeapCommitted() * 100;
    }

    public long getMemoryNonHeapCommitted() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getMemoryNonHeapCommitted();
        }
        return total;
    }

    public long getMemoryNonHeapUsed() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getMemoryNonHeapUsed();
        }
        return total;
    }

    public double getMemoryNonHeapPerc() {
        return ((double) getMemoryNonHeapUsed()) / getDetails().getMemoryNonHeapMax() * 100;
    }

    public double getMemoryNonHeapCommittedPerc() {
        return ((double) getMemoryNonHeapUsed()) / getMemoryNonHeapCommitted() * 100;
    }

    public int getThreadCount() {
        int total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getThreadCount();
        }
        return total;
    }

    public int getPeakThreadCount() {
        int total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getPeakThreadCount();
        }
        return total;
    }

    public long getGcCollectionCount() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getGcCollectionCount();
        }
        return total;
    }

    public long getGcCollectionTime() {
        long total = 0;
        for (VirtualMachineStatistics stats : virutualMachinesStatistics) {
            total += stats.getGcCollectionTime();
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
