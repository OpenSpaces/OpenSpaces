package org.openspaces.admin.vm.events;

/**
 * @author kimchy
 */
public interface VirtualMachineStatisticsChangedEventListener {

    void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event);
}