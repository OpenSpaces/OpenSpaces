package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportDetails {

    String getHost();

    int getPort();

    int getMinThreads();

    int getMaxThreads();
}
