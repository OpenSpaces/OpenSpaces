package org.openspaces.admin.machine.events;

/**
 * @author kimchy
 */
public interface MachineRemovedEventManager {

    void add(MachineRemovedEventListener eventListener);

    void remove(MachineRemovedEventListener eventListener);
}
