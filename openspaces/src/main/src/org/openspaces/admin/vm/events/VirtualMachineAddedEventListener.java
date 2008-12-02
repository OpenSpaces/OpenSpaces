package org.openspaces.admin.vm.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface VirtualMachineAddedEventListener extends AdminEventListener {

    void virtualMachineAdded(VirtualMachine virtualMachine);
}