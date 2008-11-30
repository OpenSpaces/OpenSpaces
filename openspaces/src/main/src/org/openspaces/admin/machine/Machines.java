package org.openspaces.admin.machine;

import org.openspaces.admin.Admin;

/**
 * @author kimchy
 */
public interface Machines extends Iterable<Machine> {

    Admin getAdmin();

    Machine[] getMachines();

    Machine getMachineByHost(String host);

    int size();

    void addEventListener(MachineEventListener machineListener);

    void removeEventListener(MachineEventListener machineListener);
}
