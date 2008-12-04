package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatisticsChangedEventManager {

    void add(VirtualMachinesStatisticsChangedEventListener eventListener);

    void remove(VirtualMachinesStatisticsChangedEventListener eventListener);
}