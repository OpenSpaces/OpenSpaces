package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachineRemovedEventManager {

    void add(VirtualMachineRemovedEventListener eventListener);

    void remove(VirtualMachineRemovedEventListener eventListener);
}