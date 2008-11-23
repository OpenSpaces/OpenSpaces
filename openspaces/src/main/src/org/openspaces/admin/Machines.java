package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Machines extends Iterable<Machine> {

    Machine[] getMachines();

    Machine getMachineByHost(String host);
}
