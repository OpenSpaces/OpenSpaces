package org.openspaces.admin.internal.vm;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.vm.VirtualMachine;

import java.util.Iterator;
import java.util.Map;

/**
 * @author kimchy
 */
public class DefaultVirtualMachines implements InternalVirtualMachines {

    private final Map<String, VirtualMachine> virtualMachinesByUID = new SizeConcurrentHashMap<String, VirtualMachine>();

    public VirtualMachine[] getVirtualMachines() {
        return virtualMachinesByUID.values().toArray(new VirtualMachine[0]);
    }

    public Iterator<VirtualMachine> iterator() {
        return virtualMachinesByUID.values().iterator();
    }

    public int size() {
        return virtualMachinesByUID.size();
    }

    public VirtualMachine getVirtualMachineByUID(String uid) {
        return virtualMachinesByUID.get(uid);
    }

    public void addVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachinesByUID.put(virtualMachine.getUID(), virtualMachine);
    }

    public void removeVirtualMachine(String uid) {
        virtualMachinesByUID.remove(uid);
    }
}
