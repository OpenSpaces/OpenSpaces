package org.openspaces.admin;

import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface Admin {

    void setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit);

    void close();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();

    Machines getMachines();

    Transports getTransports();

    VirtualMachines getVirtualMachines();

    ProcessingUnits getProcessingUnits();
}
