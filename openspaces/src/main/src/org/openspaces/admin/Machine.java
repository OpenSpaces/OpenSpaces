package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Machine {

    String getUID();

    String getHost();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();
}
