package org.openspaces.admin.machine.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.machine.Machine;

/**
 * @author kimchy
 */
public interface MachineAddedEventListener extends AdminEventListener {

    void machineAdded(Machine machine);
}
