package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface Transport {

    String getUID();

    String getLocalHostAddress();

    String getLocalHostName();

    String getHost();

    int getPort();

    TransportDetails getDetails();

    TransportStatistics getStatistics();
}
