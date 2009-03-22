package org.openspaces.admin.internal.vm;

import com.gigaspaces.jvm.JVMDetails;
import org.openspaces.admin.support.StatisticsUtils;
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

    public String getUid() {
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

    public long getPid() {
        return details.getPid();
    }

    public long getMemoryHeapInitInBytes() {
        return details.getMemoryHeapInit();
    }

    public double getMemoryHeapInitInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapInitInBytes());
    }

    public double getMemoryHeapInitInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapInitInBytes());
    }

    public long getMemoryHeapMaxInBytes() {
        return details.getMemoryHeapMax();
    }

    public double getMemoryHeapMaxInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapMaxInBytes());
    }

    public double getMemoryHeapMaxInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapMaxInBytes());
    }

    public long getMemoryNonHeapInitInBytes() {
        return details.getMemoryNonHeapInit();
    }

    public double getMemoryNonHeapInitInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapInitInBytes());
    }

    public double getMemoryNonHeapInitInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapInitInBytes());
    }

    public long getMemoryNonHeapMaxInBytes() {
        return details.getMemoryNonHeapMax();
    }

    public double getMemoryNonHeapMaxInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapMaxInBytes());
    }

    public double getMemoryNonHeapMaxInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapMaxInBytes());
    }
}
