package org.openspaces.admin.vm.events;

import org.openspaces.admin.vm.VirtualMachines;
import org.openspaces.admin.vm.VirtualMachinesStatistics;

/**
 * @author kimchy
 */
public class VirtualMachinesStatisticsChangedEvent {

    private final VirtualMachines virtualMachines;

    private final VirtualMachinesStatistics statistics;

    public VirtualMachinesStatisticsChangedEvent(VirtualMachines virtualMachines, VirtualMachinesStatistics statistics) {
        this.virtualMachines = virtualMachines;
        this.statistics = statistics;
    }

    public VirtualMachines getVirtualMachines() {
        return virtualMachines;
    }

    public VirtualMachinesStatistics getStatistics() {
        return statistics;
    }
}