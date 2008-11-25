package org.openspaces.admin;

import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

/**
 * @author kimchy
 */
public interface Admin {

    void close();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();

    Machines getMachines();

    Transports getTransports();

    VirtualMachines getVirtualMachines();
}
