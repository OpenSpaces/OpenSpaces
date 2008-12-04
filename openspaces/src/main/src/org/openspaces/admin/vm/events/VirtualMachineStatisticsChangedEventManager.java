package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachineStatisticsChangedEventManager {

    void add(VirtualMachineStatisticsChangedEventListener eventListener);

    void remove(VirtualMachineStatisticsChangedEventListener eventListener);
}