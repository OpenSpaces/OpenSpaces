package org.openspaces.admin.internal.vm;

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

    public long getMemoryHeapInit() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryHeapInit();
        }
        return total;
    }

    public long getMemoryHeapMax() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryHeapMax();
        }
        return total;
    }

    public long getMemoryNonHeapInit() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryNonHeapInit();
        }
        return total;
    }

    public long getMemoryNonHeapMax() {
        long total = 0;
        for (VirtualMachineDetails detail : details) {
            total += detail.getMemoryNonHeapMax();
        }
        return total;
    }
}
