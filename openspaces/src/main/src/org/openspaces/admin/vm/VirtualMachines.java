package org.openspaces.admin.vm;

import java.util.Map;

/**
 * @author kimchy
 */
public interface VirtualMachines extends Iterable<VirtualMachine> {

    VirtualMachine[] getVirtualMachines();

    VirtualMachine getVirtualMachineByUID(String uid);

    Map<String, VirtualMachine> getUids();

    int getSize();

    boolean isEmpty();

    void addEventListener(VirtualMachineEventListener eventListener);

    void removeEventListener(VirtualMachineEventListener eventListener);
}
