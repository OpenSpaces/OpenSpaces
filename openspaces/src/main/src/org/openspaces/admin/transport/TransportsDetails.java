package org.openspaces.admin.transport;

/**
 * @author kimchy
 */
public interface TransportsDetails {

    int getMinThreads();

    int getMaxThreads();
}
