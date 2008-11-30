package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachineEventListener {

    void virtualMachineAdded(VirtualMachine virtualMachine);

    void virtualMachineRemoved(VirtualMachine virtualMachine);
}
