package org.openspaces.admin.events;

/**
 * @author kimchy
 */
public interface MachineRemovedEventListener {

    void machineRemoved(MachineRemovedEvent event);
}