package org.openspaces.admin.machine.events;

import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public interface MachineAddedEventListener {

    void machineAdded(Machine machine);
}
