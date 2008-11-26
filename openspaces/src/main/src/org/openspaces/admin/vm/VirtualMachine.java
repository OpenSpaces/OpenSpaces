package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachine {

    String getUID();

    VirtualMachineDetails getDetails();

    VirtualMachineStatistics getStatistics();
}
