package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachineDetails {

    boolean isNA();

    String getUid();

    String getVmName();

    String getVmVersion();

    String getVmVendor();

    long getStartTime();

    long getMemoryHeapInitInBytes();
    double getMemoryHeapInitInMB();
    double getMemoryHeapInitInGB();

    long getMemoryHeapMaxInBytes();
    double getMemoryHeapMaxInMB();
    double getMemoryHeapMaxInGB();

    long getMemoryNonHeapInitInBytes();
    double getMemoryNonHeapInitInMB();
    double getMemoryNonHeapInitInGB();

    long getMemoryNonHeapMaxInBytes();
    double getMemoryNonHeapMaxInMB();
    double getMemoryNonHeapMaxInGB();
}
