package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureVirtualMachineRemovedEventListener extends AbstractClosureEventListener implements VirtualMachineRemovedEventListener {

    public ClosureVirtualMachineRemovedEventListener(Object closure) {
        super(closure);
    }

    public void virtualMachineRemoved(VirtualMachine virtualMachine) {
        getClosure().call(virtualMachine);
    }
}