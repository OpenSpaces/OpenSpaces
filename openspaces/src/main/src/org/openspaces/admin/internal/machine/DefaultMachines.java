package org.openspaces.admin.internal.machine;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.MachineEventListener;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultMachines implements InternalMachines {

    private final InternalAdmin admin;

    private final Map<String, Machine> machinesById = new SizeConcurrentHashMap<String, Machine>();

    private final Map<String, Machine> machinesByHost = new ConcurrentHashMap<String, Machine>();

    private final List<MachineEventListener> listeners = new CopyOnWriteArrayList<MachineEventListener>();

    public DefaultMachines(InternalAdmin admin) {
        this.admin = admin;
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public Machine[] getMachines() {
        return machinesById.values().toArray(new Machine[0]);
    }

    public int getSize() {
        return machinesById.size();
    }

    public boolean isEmpty() {
        return machinesById.size() == 0;
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

    public Map<String, Machine> getUids() {
        return Collections.unmodifiableMap(machinesById);
    }

    public Map<String, Machine> getHosts() {
        return Collections.unmodifiableMap(machinesByHost);
    }

    public void addMachine(final InternalMachine machine) {
        machinesByHost.put(machine.getHost(), machine);
        Machine existingMachine = machinesById.put(machine.getUid(), machine);
        if (existingMachine == null) {
            for (final MachineEventListener listener : listeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.machineAdded(machine);
                    }
                });
            }
        }
    }

    public void removeMachine(final InternalMachine machine) {
        machinesByHost.remove(machine.getHost());
        final Machine existingMachine = machinesById.remove(machine.getUid());
        if (existingMachine != null) {
            for (final MachineEventListener listener : listeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.machineRemoved(existingMachine);
                    }
                });
            }
        }
    }

    public void addEventListener(final MachineEventListener machineListener) {
        admin.raiseEvent(machineListener, new Runnable() {
            public void run() {
                for (Machine machine : getMachines()) {
                    machineListener.machineAdded(machine);
                }
            }
        });
        listeners.add(machineListener);
    }

    public void removeEventListener(MachineEventListener machineListener) {
        listeners.remove(machineListener);
    }
}
