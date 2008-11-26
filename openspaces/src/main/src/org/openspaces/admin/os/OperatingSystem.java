package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystem {

    String getUID();

    OperatingSystemDetails getDetails();

    OperatingSystemStatistics getStatistics();
}
