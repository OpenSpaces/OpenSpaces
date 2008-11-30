package org.openspaces.admin.internal.machine;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.MachineEvent;
import org.openspaces.admin.machine.MachineEventListener;

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

    public void addMachine(final InternalMachine machine) {
        machinesByHost.put(machine.getHost(), machine);
        Machine existingMachine = machinesById.put(machine.getUID(), machine);
        if (existingMachine == null) {
            for (final MachineEventListener listener : listeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.machineAdded(new MachineEvent(machine));
                    }
                });
            }
        }
    }

    public void removeMachine(final InternalMachine machine) {
        machinesByHost.remove(machine.getHost());
        final Machine existingMachine = machinesById.remove(machine.getUID());
        if (existingMachine != null) {
            for (final MachineEventListener listener : listeners) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.machineRemoved(new MachineEvent(existingMachine));
                    }
                });
            }
        }
    }

    public void addEventListener(final MachineEventListener machineListener) {
        admin.raiseEvent(machineListener, new Runnable() {
            public void run() {
                for (Machine machine : getMachines()) {
                    machineListener.machineAdded(new MachineEvent(machine));
                }
            }
        });
        listeners.add(machineListener);
    }

    public void removeEventListener(MachineEventListener machineListener) {
        listeners.remove(machineListener);
    }
}
