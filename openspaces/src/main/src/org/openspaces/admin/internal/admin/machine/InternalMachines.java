package org.openspaces.admin.internal.admin.machine;

import org.openspaces.admin.Machine;
import org.openspaces.admin.Machines;

/**
 * @author kimchy
 */
public interface InternalMachines extends Machines {

    Machine getMachineByUID(String uid);
    
    void addMachine(InternalMachine machine);

    void removeMachine(InternalMachine machine);
}
