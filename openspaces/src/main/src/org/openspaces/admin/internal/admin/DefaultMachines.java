package org.openspaces.admin.internal.admin;

import org.openspaces.admin.Machine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultMachines implements InternalMachines {

    private final Map<String, InternalMachine> machinesById = new ConcurrentHashMap<String, InternalMachine>();

    private final Map<String, InternalMachine> machinesByHost = new ConcurrentHashMap<String, InternalMachine>();

    public Machine[] getMachines() {
        return machinesById.values().toArray(new Machine[0]);
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
