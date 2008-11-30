package org.openspaces.admin.internal.vm;

import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachines;

/**
 * @author kimchy
 */
public interface InternalVirtualMachines extends VirtualMachines {

    void addVirtualMachine(VirtualMachine virtualMachine);

    InternalVirtualMachine removeVirtualMachine(String uid);
}
