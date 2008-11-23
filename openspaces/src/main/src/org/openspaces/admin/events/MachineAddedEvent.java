package org.openspaces.admin.events;

import org.openspaces.admin.Machine;

/**
 * @author kimchy
 */
public class MachineAddedEvent extends MachineEvent {

    public MachineAddedEvent(Machine machine) {
        super(machine);
    }
}
