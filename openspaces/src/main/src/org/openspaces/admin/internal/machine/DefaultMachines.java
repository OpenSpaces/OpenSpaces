package org.openspaces.admin.internal.machine;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.machine.events.DefaultMachineAddedEventManager;
import org.openspaces.admin.internal.machine.events.DefaultMachineRemovedEventManager;
import org.openspaces.admin.internal.machine.events.InternalMachineAddedEventManager;
import org.openspaces.admin.internal.machine.events.InternalMachineRemovedEventManager;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineAddedEventManager;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultMachines implements InternalMachines {

    private final InternalAdmin admin;

    private final InternalMachineAddedEventManager machineAddedEventManager;

    private final InternalMachineRemovedEventManager machineRemovedEventManager;

    private final Map<String, Machine> machinesById = new SizeConcurrentHashMap<String, Machine>();

    private final Map<String, Machine> machinesByHost = new ConcurrentHashMap<String, Machine>();

    public DefaultMachines(InternalAdmin admin) {
        this.admin = admin;
        this.machineAddedEventManager = new DefaultMachineAddedEventManager(this);
        this.machineRemovedEventManager = new DefaultMachineRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public Machine[] getMachines() {
        return machinesById.values().toArray(new Machine[0]);
    }

    public MachineAddedEventManager getMachineAdded() {
        return this.machineAddedEventManager;
    }

    public MachineRemovedEventManager getMachineRemoved() {
        return this.machineRemovedEventManager;
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

    public void addLifecycleListener(MachineLifecycleEventListener eventListener) {
        getMachineAdded().add(eventListener);
        getMachineRemoved().add(eventListener);
    }

    public void removeLifeycleListener(MachineLifecycleEventListener eventListener) {
        getMachineAdded().remove(eventListener);
        getMachineRemoved().remove(eventListener);
    }

    public void addMachine(final InternalMachine machine) {
        machinesByHost.put(machine.getHost(), machine);
        Machine existingMachine = machinesById.put(machine.getUid(), machine);
        if (existingMachine == null) {
            machineAddedEventManager.machineAdded(machine);
        }
    }

    public void removeMachine(final Machine machine) {
        machinesByHost.remove(machine.getHost());
        final Machine existingMachine = machinesById.remove(machine.getUid());
        if (existingMachine != null) {
            machineRemovedEventManager.machineRemoved(machine);
        }
    }
}
