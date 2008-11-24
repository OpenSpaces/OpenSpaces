package org.openspaces.admin.internal.admin;

import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.GridServiceContainers;
import org.openspaces.admin.GridServiceManagers;
import org.openspaces.admin.LookupServices;
import org.openspaces.admin.Machines;
import org.openspaces.admin.internal.discovery.DiscoveryService;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalMachines machines = new DefaultMachines();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    public DefaultAdmin(String[] groups, LookupLocator[] locators) {
        this.discoveryService = new DiscoveryService(groups, locators, this);
    }

    public void start() {
        discoveryService.start();
    }

    public void stop() {
        discoveryService.stop();
    }

    public LookupServices getLookupServices() {
        return this.lookupServices;
    }

    public GridServiceManagers getGridServiceManagers() {
        return this.gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return this.gridServiceContainers;
    }

    public Machines getMachines() {
        return this.machines;
    }

    public synchronized void addLookupService(InternalLookupService lookupService) {
        lookupServices.addLookupService(lookupService);
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(lookupService.getTransportConfiguration().getHost());
        if (machine == null) {
            machine = new DefaultMachine(lookupService.getTransportConfiguration().getHost(), lookupService.getTransportConfiguration().getHost());
            machines.addMachine(machine);
        }
        lookupService.setMachine(machine);
        machine.addLookupService(lookupService);
    }

    public synchronized void removeLookupService(String uid) {
        InternalLookupService lookupService = lookupServices.removeLookupService(uid);
        if (lookupService != null) {
            ((InternalMachine) lookupService.getMachine()).removeLookupService(uid);
        }
    }

    public synchronized void addGridServiceManager(InternalGridServiceManager gridServiceManager) {
        gridServiceManagers.addGridServiceManager(gridServiceManager);
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(gridServiceManager.getTransportConfiguration().getHost());
        if (machine == null) {
            machine = new DefaultMachine(gridServiceManager.getTransportConfiguration().getHost(), gridServiceManager.getTransportConfiguration().getHost());
            machines.addMachine(machine);
        }
        gridServiceManager.setMachine(machine);
        machine.addGridServiceManager(gridServiceManager);
    }

    public synchronized void removeGridServiceManager(String uid) {
        InternalGridServiceManager gridServiceManager = gridServiceManagers.removeGridServiceManager(uid);
        if (gridServiceManager != null) {
            ((InternalMachine) gridServiceManager.getMachine()).removeGridServiceManager(uid);
        }
    }

    public synchronized void replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        InternalGridServiceManager oldGridServiceManager = gridServiceManagers.replaceGridServiceManager(gridServiceManager);
        if (oldGridServiceManager != null) {
            ((InternalMachine) oldGridServiceManager.getMachine()).replaceGridServiceManager(gridServiceManager);
        }
    }

    public synchronized void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        gridServiceContainers.addGridServiceContainer(gridServiceContainer);
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(gridServiceContainer.getTransportConfiguration().getHost());
        if (machine == null) {
            machine = new DefaultMachine(gridServiceContainer.getTransportConfiguration().getHost(), gridServiceContainer.getTransportConfiguration().getHost());
            machines.addMachine(machine);
        }
        gridServiceContainer.setMachine(machine);
        machine.addGridServiceContainer(gridServiceContainer);
    }

    public synchronized void removeGridServiceContainer(String uid) {
        InternalGridServiceContainer gridServiceContainer = gridServiceContainers.removeGridServiceContainer(uid);
        if (gridServiceContainer != null) {
            ((InternalMachine) gridServiceContainer.getMachine()).removeGridServiceContainer(uid);
        }
    }

    public synchronized void repalceGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        InternalGridServiceContainer oldGridServiceContainer = gridServiceContainers.replaceGridServiceContainer(gridServiceContainer);
        if (oldGridServiceContainer != null) {
            ((InternalMachine) oldGridServiceContainer.getMachine()).replaceGridServiceContainer(gridServiceContainer);
        }
    }
}
