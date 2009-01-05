package org.openspaces.admin.vm.events;

import org.openspaces.admin.AdminEventListener;

/**
 * @author kimchy
 */
public interface VirtualMachinesStatisticsChangedEventListener extends AdminEventListener {

    void virtualMachinesStatisticsChanged(VirtualMachinesStatisticsChangedEvent event);
}