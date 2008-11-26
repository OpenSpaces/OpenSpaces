package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachineDetails {

    boolean isNA();

    String getUID();

    String getVmName();

    String getVmVersion();

    String getVmVendor();

    long getStartTime();

    long getMemoryHeapInit();

    long getMemoryHeapMax();

    long getMemoryNonHeapInit();

    long getMemoryNonHeapMax();
}
