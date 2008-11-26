package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMStatistics;
import org.openspaces.admin.vm.VirtualMachineStatistics;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatistics implements VirtualMachineStatistics {

    private static final JVMStatistics NA_STATS = new JVMStatistics();

    private final JVMStatistics stats;

    public DefaultVirtualMachineStatistics() {
        this.stats = NA_STATS;
    }

    public DefaultVirtualMachineStatistics(JVMStatistics stats) {
        this.stats = stats;
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
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

    public long getMemoryNonHeapCommitted() {
        return stats.getMemoryNonHeapCommitted();
    }

    public long getMemoryNonHeapUsed() {
        return stats.getMemoryNonHeapUsed();
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
}
