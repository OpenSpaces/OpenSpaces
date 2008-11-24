package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportAware {

    Transport getTransport();
}
