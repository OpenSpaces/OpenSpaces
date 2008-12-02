package org.openspaces.admin.vm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface VirtualMachines extends AdminAware, Iterable<VirtualMachine> {

    VirtualMachine[] getVirtualMachines();

    VirtualMachine getVirtualMachineByUID(String uid);

    Map<String, VirtualMachine> getUids();

    int getSize();

    boolean isEmpty();

    void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener);

    VirtualMachineAddedEventManager getVirtualMachineAdded();

    VirtualMachineRemovedEventManager getVirtualMachineRemoved();
}
