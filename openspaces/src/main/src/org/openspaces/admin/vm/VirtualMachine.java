package org.openspaces.admin.vm;

import org.openspaces.admin.machine.MachineAware;

/**
 * @author kimchy
 */
public interface VirtualMachine extends MachineAware {

    String getUID();

    VirtualMachineDetails getDetails();

    VirtualMachineStatistics getStatistics();
}
