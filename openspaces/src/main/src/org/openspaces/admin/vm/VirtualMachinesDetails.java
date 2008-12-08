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
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapInit()}
     */
    long getMemoryHeapInit();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryHeapMax()}
     */
    long getMemoryHeapMax();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapInit()}
     */
    long getMemoryNonHeapInit();

    /**
     * Retuns an aggregation of all the different virtual machines {@link VirtualMachineDetails#getMemoryNonHeapMax()}
     */
    long getMemoryNonHeapMax();
}
