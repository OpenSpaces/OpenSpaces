package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface GridServiceManager extends TransportInfoProvider {

    String getUID();

    Machine getMachine();
}
