package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Admin {

    void start();

    void stop();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();

    Machines getMachines();
}
