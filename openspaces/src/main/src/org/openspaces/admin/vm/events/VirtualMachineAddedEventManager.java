package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachineAddedEventManager {

    void add(VirtualMachineAddedEventListener eventListener);

    void remove(VirtualMachineAddedEventListener eventListener);

}