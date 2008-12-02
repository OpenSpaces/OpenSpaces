package org.openspaces.admin.internal.vm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineAddedEventManager;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineRemovedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineAddedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineRemovedEventManager;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineLifecycleEventListener;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultVirtualMachines implements InternalVirtualMachines {

    private final InternalAdmin admin;

    private final Map<String, VirtualMachine> virtualMachinesByUID = new SizeConcurrentHashMap<String, VirtualMachine>();

    private final InternalVirtualMachineAddedEventManager virtualMachineAddedEventManager;

    private final InternalVirtualMachineRemovedEventManager virtualMachineRemovedEventManager;

    public DefaultVirtualMachines(InternalAdmin admin) {
        this.admin = admin;
        this.virtualMachineAddedEventManager = new DefaultVirtualMachineAddedEventManager(this);
        this.virtualMachineRemovedEventManager = new DefaultVirtualMachineRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public VirtualMachineAddedEventManager getVirtualMachineAdded() {
        return this.virtualMachineAddedEventManager;
    }

    public VirtualMachineRemovedEventManager getVirtualMachineRemoved() {
        return this.virtualMachineRemovedEventManager;
    }

    public VirtualMachine[] getVirtualMachines() {
        return virtualMachinesByUID.values().toArray(new VirtualMachine[0]);
    }

    public Iterator<VirtualMachine> iterator() {
        return virtualMachinesByUID.values().iterator();
    }

    public int getSize() {
        return virtualMachinesByUID.size();
    }

    public boolean isEmpty() {
        return virtualMachinesByUID.size() == 0;
    }

    public void addLifecycleListener(VirtualMachineLifecycleEventListener eventListener) {
        getVirtualMachineAdded().add(eventListener);
        getVirtualMachineRemoved().add(eventListener);
    }

    public void removeLifecycleListener(VirtualMachineLifecycleEventListener eventListener) {
        getVirtualMachineAdded().remove(eventListener);
        getVirtualMachineRemoved().remove(eventListener);
    }

    public VirtualMachine getVirtualMachineByUID(String uid) {
        return virtualMachinesByUID.get(uid);
    }

    public Map<String, VirtualMachine> getUids() {
        return Collections.unmodifiableMap(virtualMachinesByUID);
    }

    public void addVirtualMachine(final VirtualMachine virtualMachine) {
        VirtualMachine existingVM = virtualMachinesByUID.put(virtualMachine.getUid(), virtualMachine);
        if (existingVM == null) {
            virtualMachineAddedEventManager.virtualMachineAdded(virtualMachine);
        }
    }

    public InternalVirtualMachine removeVirtualMachine(String uid) {
        final InternalVirtualMachine existingVM = (InternalVirtualMachine) virtualMachinesByUID.remove(uid);
        if (existingVM != null) {
            virtualMachineRemovedEventManager.virtualMachineRemoved(existingVM);
        }
        return existingVM;
    }
}
