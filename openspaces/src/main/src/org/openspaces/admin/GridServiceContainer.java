package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface GridServiceContainer extends MachineAware, TransportAware {

    String getUID();

}