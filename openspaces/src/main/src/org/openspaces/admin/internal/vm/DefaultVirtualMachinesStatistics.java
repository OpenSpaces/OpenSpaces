package org.openspaces.admin.internal.vm;

import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.VirtualMachinesStatistics;

/**
 * @author kimchy
 */
public class DefaultVirtualMachinesStatistics implements VirtualMachinesStatistics {

    private final long timestamp;

    private final VirtualMachineStatistics[] virutualMachinesStatistics;

    public DefaultVirtualMachinesStatistics(VirtualMachineStatistics[] virutualMachinesStatistics) {
        this.timestamp = System.currentTimeMillis();
        this.virutualMachinesStatistics = virutualMachinesStatistics;
    }

    public boolean isNA() {
        return virutualMachinesStatistics == null || virutualMachinesStatistics[0].isNA();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSize() {
        return virutualMachinesStatistics.length;
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
}
