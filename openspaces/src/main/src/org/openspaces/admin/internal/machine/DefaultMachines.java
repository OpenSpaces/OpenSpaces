package org.openspaces.admin.internal.machine;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.machine.Machine;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultMachines implements InternalMachines {

    private final Map<String, Machine> machinesById = new SizeConcurrentHashMap<String, Machine>();

    private final Map<String, Machine> machinesByHost = new ConcurrentHashMap<String, Machine>();

    public Machine[] getMachines() {
        return machinesById.values().toArray(new Machine[0]);
    }

    public int size() {
        return machinesById.size();
    }

    public Iterator<Machine> iterator() {
        return machinesById.values().iterator();
    }

    public Machine getMachineByUID(String uid) {
        return machinesById.get(uid);
    }

    public Machine getMachineByHost(String ipAddress) {
        return machinesByHost.get(ipAddress);
    }

    public void addMachine(InternalMachine machine) {
        machinesByHost.put(machine.getHost(), machine);
        machinesById.put(machine.getUID(), machine);
    }

    public void removeMachine(InternalMachine machine) {
        machinesByHost.remove(machine.getHost());
        machinesById.remove(machine.getUID());
    }
}
