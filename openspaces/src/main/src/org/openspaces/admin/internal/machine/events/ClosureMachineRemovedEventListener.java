package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureMachineRemovedEventListener extends AbstractClosureEventListener implements MachineRemovedEventListener {

    public ClosureMachineRemovedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void machineRemoved(Machine machine) {
        getClosure().call(machine);
    }
}