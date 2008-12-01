package org.openspaces.admin.machine.events;

import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public interface MachineRemovedEventListener {

    void machineRemoved(Machine machine);
}
