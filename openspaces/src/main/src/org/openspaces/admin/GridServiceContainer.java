package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface GridServiceContainer extends TransportInfoProvider {

    String getUID();

    Machine getMachine();
}