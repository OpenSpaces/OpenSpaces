package org.openspaces.admin.vm.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface VirtualMachineStatisticsChangedEventListener extends AdminEventListener {

    void virtualMachineStatisticsChanged(VirtualMachineStatisticsChangedEvent event);
}