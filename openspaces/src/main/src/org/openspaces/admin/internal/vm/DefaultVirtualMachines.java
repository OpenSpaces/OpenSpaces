package org.openspaces.admin.internal.vm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineEventListener;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultVirtualMachines implements InternalVirtualMachines {

    private final InternalAdmin admin;

    private final Map<String, VirtualMachine> virtualMachinesByUID = new SizeConcurrentHashMap<String, VirtualMachine>();

    private final List<VirtualMachineEventListener> eventListeners = new CopyOnWriteArrayList<VirtualMachineEventListener>();

    public DefaultVirtualMachines(InternalAdmin admin) {
        this.admin = admin;
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

    public VirtualMachine getVirtualMachineByUID(String uid) {
        return virtualMachinesByUID.get(uid);
    }

    public Map<String, VirtualMachine> getUids() {
        return Collections.unmodifiableMap(virtualMachinesByUID);
    }

    public void addVirtualMachine(final VirtualMachine virtualMachine) {
        VirtualMachine existingVM = virtualMachinesByUID.put(virtualMachine.getUid(), virtualMachine);
        if (existingVM == null) {
            for (final VirtualMachineEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.virtualMachineAdded(virtualMachine);
                    }
                });
            }
        }
    }

    public InternalVirtualMachine removeVirtualMachine(String uid) {
        final InternalVirtualMachine existingVM = (InternalVirtualMachine) virtualMachinesByUID.remove(uid);
        if (existingVM != null) {
            for (final VirtualMachineEventListener eventListener : eventListeners) {
                admin.pushEvent(eventListener, new Runnable() {
                    public void run() {
                        eventListener.virtualMachineRemoved(existingVM);
                    }
                });
            }
        }
        return existingVM;
    }

    public void addEventListener(final VirtualMachineEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (VirtualMachine virtualMachine : getVirtualMachines()) {
                    eventListener.virtualMachineAdded(virtualMachine);
                }
            }
        });
        eventListeners.add(eventListener);
    }

    public void removeEventListener(VirtualMachineEventListener eventListener) {
        eventListeners.remove(eventListener);
    }
}
