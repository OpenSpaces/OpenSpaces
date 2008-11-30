package org.openspaces.admin.machine;

/**
 * @author kimchy
 */
public interface MachineEventListener {

    void machineAdded(Machine machine);

    void machineRemoved(Machine machine);
}
