package org.openspaces.admin.machine;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.machine.events.MachineAddedEventManager;
import org.openspaces.admin.machine.events.MachineRemovedEventManager;

import java.util.Map;

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

    MachineAddedEventManager getMachineAdded();

    MachineRemovedEventManager getMachineRemoved();
}
