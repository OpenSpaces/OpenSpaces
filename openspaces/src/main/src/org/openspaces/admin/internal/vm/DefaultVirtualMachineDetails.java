package org.openspaces.admin.internal.vm;

import java.util.Map;

import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineDetails;

import com.gigaspaces.internal.jvm.JVMDetails;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineDetails implements VirtualMachineDetails {

    private final JVMDetails details;
    private final String jmxUrl;

    public DefaultVirtualMachineDetails() {
        this( new JVMDetails(), "" );
    }

    public DefaultVirtualMachineDetails(JVMDetails details,String jmxUrl) {
        this.details = details;
        this.jmxUrl = jmxUrl;
    }
    
    public String getJmxUrl(){
        return jmxUrl;
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

    public String getBootClassPath() {
        return details.getBootClassPath();
    }

    public String getClassPath() {
        return details.getClassPath();
    }

    public String[] getInputArguments() {
        return details.getInputArguments();
    }

    public Map<String, String> getSystemProperties() {
        return details.getSystemProperties();
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
