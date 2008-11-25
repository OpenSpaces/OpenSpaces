package org.openspaces.admin.internal.vm;

import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineAware;

/**
 * @author kimchy
 */
public interface InternalVirtualMachineAware extends VirtualMachineAware {

    void setVirtualMachine(VirtualMachine virtualMachine);
}