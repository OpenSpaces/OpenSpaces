package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Transports extends Iterable<Transport> {

    Transport[] getTransports();

    Transport[] getTransports(String host);

    Transport getTransportByHostAndPort(String host, int port);

    Transport getTransportByUID(String uid);

    int size();
}
