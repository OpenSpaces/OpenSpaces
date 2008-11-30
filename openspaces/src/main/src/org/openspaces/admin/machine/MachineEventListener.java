package org.openspaces.admin.machine;

/**
 * @author kimchy
 */
public interface MachineEventListener {

    void machineAdded(MachineEvent machineEvent);

    void machineRemoved(MachineEvent machineEvent);
}
