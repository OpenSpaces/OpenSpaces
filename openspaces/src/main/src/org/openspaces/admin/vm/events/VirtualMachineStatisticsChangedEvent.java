package org.openspaces.admin.vm.events;

import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineStatistics;

/**
 * @author kimchy
 */
public class VirtualMachineStatisticsChangedEvent {

    private final VirtualMachine virtualMachine;

    private final VirtualMachineStatistics statistics;

    public VirtualMachineStatisticsChangedEvent(VirtualMachine virtualMachine, VirtualMachineStatistics statistics) {
        this.virtualMachine = virtualMachine;
        this.statistics = statistics;
    }

    public VirtualMachine getVirtualMachine() {
        return virtualMachine;
    }

    public VirtualMachineStatistics getStatistics() {
        return statistics;
    }
}