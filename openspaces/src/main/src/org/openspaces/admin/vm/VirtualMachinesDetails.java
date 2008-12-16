package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachinesDetails {

    int getSize();

    String[] getVmName();

    String[] getVmVersion();

    String[] getVmVendor();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapInitInBytes()}
     */
    long getMemoryHeapInitInBytes();
    double getMemoryHeapInitInMB();
    double getMemoryHeapInitInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapMaxInBytes()}
     */
    long getMemoryHeapMaxInBytes();
    double getMemoryHeapMaxInMB();
    double getMemoryHeapMaxInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapInitInBytes()}
     */
    long getMemoryNonHeapInitInBytes();
    double getMemoryNonHeapInitInMB();
    double getMemoryNonHeapInitInGB();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapMaxInBytes()}
     */
    long getMemoryNonHeapMaxInBytes();
    double getMemoryNonHeapMaxInMB();
    double getMemoryNonHeapMaxInGB();
}
