package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureMachineAddedEventListener extends AbstractClosureEventListener implements MachineAddedEventListener {

    public ClosureMachineAddedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void machineAdded(Machine machine) {
        getClosure().call(machine);
    }
}
