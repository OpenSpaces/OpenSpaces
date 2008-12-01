package org.openspaces.admin.vm.events;

import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface VirtualMachineRemovedEventListener {

    void virtualMachineRemoved(VirtualMachine virtualMachine);
}