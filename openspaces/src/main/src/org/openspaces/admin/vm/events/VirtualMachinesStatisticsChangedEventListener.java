package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatisticsChangedEventListener {

    void virtualMachinesStatisticsChanged(VirtualMachinesStatisticsChangedEvent event);
}