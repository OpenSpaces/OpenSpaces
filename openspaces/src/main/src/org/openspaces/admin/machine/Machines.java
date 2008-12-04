package org.openspaces.admin.machine;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.machine.events.MachineAddedEventManager;
import org.openspaces.admin.machine.events.MachineLifecycleEventListener;
import org.openspaces.admin.machine.events.MachineRemovedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface Machines extends AdminAware, Iterable<Machine> {

    Machine[] getMachines();

    Machine getMachineByHost(String host);

    Map<String, Machine> getUids();

    Map<String, Machine> getHosts();

    int getSize();

    boolean isEmpty();

    /**
     * Waits till at least the provided number of Machines are up.
     */
    boolean waitFor(int numberOfMachines);

    /**
     * Waits till at least the provided number of Machines are up for the specified timeout.
     */
    boolean waitFor(int numberOfMachines, long timeout, TimeUnit timeUnit);
    
    void addLifecycleListener(MachineLifecycleEventListener eventListener);

    void removeLifeycleListener(MachineLifecycleEventListener eventListener);

    MachineAddedEventManager getMachineAdded();

    MachineRemovedEventManager getMachineRemoved();
}
