package org.openspaces.admin.internal.machine;

import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.Machines;

/**
 * @author kimchy
 */
public interface InternalMachines extends Machines {

    Machine getMachineByUID(String uid);
    
    void addMachine(InternalMachine machine);

    void removeMachine(Machine machine);
}
