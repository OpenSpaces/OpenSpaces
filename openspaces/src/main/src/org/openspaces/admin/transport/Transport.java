package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface Transport {

    String getUID();

    String getHost();

    int getPort();

    TransportDetails getDetails();

    TransportStatistics getStatistics();
}
