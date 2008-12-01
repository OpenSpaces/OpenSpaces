package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface Transport {

    String getUid();

    String getLocalHostAddress();

    String getLocalHostName();

    String getHost();

    int getPort();

    TransportDetails getDetails();

    TransportStatistics getStatistics();
}
