package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportDetails {

    String getHost();

    String getLocalHostAddress();

    String getLocalHostName();    

    int getPort();

    int getMinThreads();

    int getMaxThreads();
}
