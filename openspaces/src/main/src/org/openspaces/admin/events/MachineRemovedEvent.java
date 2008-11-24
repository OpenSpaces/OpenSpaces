package org.openspaces.admin.events;

import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public class MachineRemovedEvent extends MachineEvent {

    public MachineRemovedEvent(Machine machine) {
        super(machine);
    }
}