package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface LookupService extends TransportInfoProvider {

    String getUID();

    Machine getMachine();
}
