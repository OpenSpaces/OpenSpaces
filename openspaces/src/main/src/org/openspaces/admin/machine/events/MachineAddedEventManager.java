package org.openspaces.admin.machine.events;

/**
 * @author kimchy
 */
public interface MachineAddedEventManager {

    void add(MachineAddedEventListener eventListener);

    void remove(MachineAddedEventListener eventListener);

}
