package org.openspaces.admin.vm.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * @author kimchy
 */
public interface VirtualMachineRemovedEventListener extends AdminEventListener {

    void virtualMachineRemoved(VirtualMachine virtualMachine);
}