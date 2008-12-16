package org.openspaces.admin.internal.vm;

import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachinesDetails;

import java.util.HashSet;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultVirtualMachinesDetails implements VirtualMachinesDetails {

    private final VirtualMachineDetails[] details;

    public DefaultVirtualMachinesDetails(VirtualMachineDetails[] details) {
        this.details = details;
    }

    public int getSize() {
        return details.length;
    }

    public String[] getVmName() {
        Set<String> values = new HashSet<String>();
        for (VirtualMachineDetails detail : details) {
            values.add(detail.getVmName());
        }
        return values.toArray(new String[values.size()]);
    }

    public String[] getVmVersion() {
        Set<String> values = new HashSet<String>();
        for (VirtualMachineDetails detail : details) {
            values.add(detail.getVmVersion());
        }
        return values.toArray(new String[values.size()]);
    }

    public String[] getVmVendor() {
        Set<String> values = new HashSet<String>();
        for (VirtualMachineDetails detail : details) {
            values.add(detail.getVmVendor());
        }
        return values.toArray(new String[values.size()]);
    }

    public long getMemoryHeapInitInBytes() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryHeapInitInBytes();
        }
        return total;
    }

    public double getMemoryHeapInitInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapInitInBytes());
    }

    public double getMemoryHeapInitInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapInitInBytes());
    }

    public long getMemoryHeapMaxInBytes() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryHeapMaxInBytes();
        }
        return total;
    }

    public double getMemoryHeapMaxInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapMaxInBytes());
    }

    public double getMemoryHeapMaxInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapMaxInBytes());
    }

    public long getMemoryNonHeapInitInBytes() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryNonHeapInitInBytes();
        }
        return total;
    }

    public double getMemoryNonHeapInitInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapInitInBytes());
    }

    public double getMemoryNonHeapInitInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapInitInBytes());
    }

    public long getMemoryNonHeapMaxInBytes() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryNonHeapMaxInBytes();
        }
        return total;
    }

    public double getMemoryNonHeapMaxInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapMaxInBytes());
    }

    public double getMemoryNonHeapMaxInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapMaxInBytes());
    }
}
