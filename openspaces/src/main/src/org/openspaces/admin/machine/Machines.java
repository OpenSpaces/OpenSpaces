package org.openspaces.admin.machine;

/**
 * @author kimchy
 */
public interface Machines extends Iterable<Machine> {

    Machine[] getMachines();

    Machine getMachineByHost(String host);

    int size();
}
