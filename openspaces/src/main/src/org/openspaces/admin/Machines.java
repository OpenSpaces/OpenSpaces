package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Machines {

    Machine[] getMachines();

    Machine getMachineByHost(String host);
}
