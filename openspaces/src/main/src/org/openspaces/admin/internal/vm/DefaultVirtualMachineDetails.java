package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import org.openspaces.admin.vm.VirtualMachineDetails;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineDetails implements VirtualMachineDetails {

    private final JVMDetails details;

    public DefaultVirtualMachineDetails() {
        this.details = new JVMDetails();
    }

    public DefaultVirtualMachineDetails(JVMDetails details) {
        this.details = details;
    }

    public boolean isNA() {
        return details.isNA();
    }

    public String getUID() {
        return details.getUid();
    }

    public String getVmName() {
        return details.getVmName();
    }

    public String getVmVersion() {
        return details.getVmVersion();
    }

    public String getVmVendor() {
        return details.getVmVendor();
    }

    public long getStartTime() {
        return details.getStartTime();
    }

    public long getMemoryHeapInit() {
        return details.getMemoryHeapInit();
    }

    public long getMemoryHeapMax() {
        return details.getMemoryHeapMax();
    }

    public long getMemoryNonHeapInit() {
        return details.getMemoryNonHeapInit();
    }

    public long getMemoryNonHeapMax() {
        return details.getMemoryNonHeapMax();
    }
}
