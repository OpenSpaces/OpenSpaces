package org.openspaces.admin.internal.machine;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.MachineAware;

/**
 * @author kimchy
 */
public interface InternalMachineAware extends MachineAware {

    void setMachine(Machine machine);
}
