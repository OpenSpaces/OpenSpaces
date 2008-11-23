package org.openspaces.admin.events;

/**
 * @author kimchy
 */
public interface MachineAddedEventListener {

    void machineAdded(MachineAddedEvent event);
}
