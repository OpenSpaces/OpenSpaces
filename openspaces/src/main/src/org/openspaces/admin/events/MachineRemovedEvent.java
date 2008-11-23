package org.openspaces.admin.events;

import org.openspaces.admin.Machine;

/**
 * @author kimchy
 */
public class MachineRemovedEvent extends MachineEvent {

    public MachineRemovedEvent(Machine machine) {
        super(machine);
    }
}