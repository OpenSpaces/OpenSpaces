package org.openspaces.admin;

import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachines;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface Admin extends StatisticsMonitor {

    String[] getGroups();

    LookupLocator[] getLocators();

    void setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit);

    void setAgentProcessessMonitorInterval(long interval, TimeUnit timeUnit);

    void setSpaceMonitorInterval(long interval, TimeUnit timeUnit);

    void setSchedulerCorePoolSize(int coreThreads);

    void close();

    GridServiceAgents getGridServiceAgents();

    LookupServices getLookupServices();

    GridServiceManagers getGridServiceManagers();

    GridServiceContainers getGridServiceContainers();

    Machines getMachines();

    Transports getTransports();

    VirtualMachines getVirtualMachines();

    ProcessingUnits getProcessingUnits();

    Spaces getSpaces();

    /**
     * Smart addition of event listeners basde on implemented interfaces.
     */
    void addEventListener(AdminEventListener eventListener);

    void removeEventListener(AdminEventListener eventListener);
}
