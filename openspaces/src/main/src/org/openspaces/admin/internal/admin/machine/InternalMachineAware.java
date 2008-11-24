package org.openspaces.admin.internal.admin.machine;

import org.openspaces.admin.Machine;
import org.openspaces.admin.MachineAware;

/**
 * @author kimchy
 */
public interface InternalMachineAware extends MachineAware {

    void setMachine(Machine machine);
}
