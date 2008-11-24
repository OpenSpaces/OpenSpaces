package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface LookupService extends TransportAware, MachineAware {

    String getUID();
}
