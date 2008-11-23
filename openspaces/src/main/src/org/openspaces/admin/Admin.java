package org.openspaces.admin;

/**
 * @author kimchy
 */
public interface Admin {

    void start();

    void stop();

    LookupServices getLookupServices();

    Machines getMachines();
}
