package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemsStatistics {

    boolean isNA();

    long getTimestamp();

    int getSize();
}