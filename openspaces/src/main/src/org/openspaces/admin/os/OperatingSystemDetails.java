package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystemDetails {

    boolean isNA();

    String getUID();

    String getName();

    String getArch();

    String getVersion();

    int getAvailableProcessors();
}
