package org.openspaces.admin.internal.admin;

import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.LookupServices;
import org.openspaces.admin.Machines;
import org.openspaces.admin.internal.discovery.DiscoveryService;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private DiscoveryService discoveryService;

    private InternalLookupServices lookupServices = new DefaultLookupServices();

    private InternalMachines machines = new DefaultMachines();

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
}
