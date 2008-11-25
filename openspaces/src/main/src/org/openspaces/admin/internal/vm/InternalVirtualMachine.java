package org.openspaces.admin.internal.vm;

import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface InternalVirtualMachine extends VirtualMachine {

    void addVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider);

    void removeVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider);

    boolean hasVirtualMachineInfoProviders();
}
