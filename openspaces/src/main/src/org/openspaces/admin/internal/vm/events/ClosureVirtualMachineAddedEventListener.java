package org.openspaces.admin.internal.vm.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureVirtualMachineAddedEventListener extends AbstractClosureEventListener implements VirtualMachineAddedEventListener {

    public ClosureVirtualMachineAddedEventListener(Object closure) {
        super(closure);
    }

    public void virtualMachineAdded(VirtualMachine virtualMachine) {
        getClosure().call(virtualMachine);
    }
}