package org.openspaces.admin.vm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.vm.events.VirtualMachinesStatisticsChangedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface VirtualMachines extends AdminAware, Iterable<VirtualMachine>, StatisticsMonitor {

    VirtualMachine[] getVirtualMachines();

    VirtualMachinesStatistics getStatistics();

    VirtualMachine getVirtualMachineByUID(String uid);

    Map<String, VirtualMachine> getUids();

    int getSize();

    boolean isEmpty();

    void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    VirtualMachineAddedEventManager getVirtualMachineAdded();

    VirtualMachineRemovedEventManager getVirtualMachineRemoved();

    VirtualMachinesStatisticsChangedEventManager getStatisticsChanged();

    VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged();
}
