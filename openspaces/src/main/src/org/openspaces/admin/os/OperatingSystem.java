package org.openspaces.admin.os;

/**
 * @author kimchy
 */
public interface OperatingSystem {

    String getUid();

    OperatingSystemDetails getDetails();

    OperatingSystemStatistics getStatistics();
}
