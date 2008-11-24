package org.openspaces.admin.events;

import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public class MachineEvent {

    private Machine machine;

    public MachineEvent(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return machine;
    }
}
