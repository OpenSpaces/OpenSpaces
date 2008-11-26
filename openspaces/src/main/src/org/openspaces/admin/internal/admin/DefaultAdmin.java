package org.openspaces.admin.internal.admin;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.discovery.DiscoveryService;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.lus.DefaultLookupServices;
import org.openspaces.admin.internal.lus.InternalLookupService;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.machine.DefaultMachine;
import org.openspaces.admin.internal.machine.DefaultMachines;
import org.openspaces.admin.internal.machine.InternalMachine;
import org.openspaces.admin.internal.machine.InternalMachineAware;
import org.openspaces.admin.internal.machine.InternalMachines;
import org.openspaces.admin.internal.os.DefaultOperatingSystem;
import org.openspaces.admin.internal.os.DefaultOperatingSystems;
import org.openspaces.admin.internal.os.InternalOperatingSystem;
import org.openspaces.admin.internal.os.InternalOperatingSystemInfoProvider;
import org.openspaces.admin.internal.os.InternalOperatingSystems;
import org.openspaces.admin.internal.transport.DefaultTransport;
import org.openspaces.admin.internal.transport.DefaultTransports;
import org.openspaces.admin.internal.transport.InternalTransport;
import org.openspaces.admin.internal.transport.InternalTransportInfoProvider;
import org.openspaces.admin.internal.transport.InternalTransports;
import org.openspaces.admin.internal.vm.DefaultVirtualMachine;
import org.openspaces.admin.internal.vm.DefaultVirtualMachines;
import org.openspaces.admin.internal.vm.InternalVirtualMachine;
import org.openspaces.admin.internal.vm.InternalVirtualMachineInfoProvider;
import org.openspaces.admin.internal.vm.InternalVirtualMachines;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachines;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalMachines machines = new DefaultMachines();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    private final InternalTransports transports = new DefaultTransports();

    private final InternalOperatingSystems operatingSystems = new DefaultOperatingSystems();

    private final InternalVirtualMachines virtualMachines = new DefaultVirtualMachines();

    public DefaultAdmin(String[] groups, LookupLocator[] locators) {
        this.discoveryService = new DiscoveryService(groups, locators, this);
        discoveryService.start();
    }

    public void close() {
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

    public Transports getTransports() {
        return this.transports;
    }

    public VirtualMachines getVirtualMachines() {
        return this.virtualMachines;
    }

    public synchronized void addLookupService(InternalLookupService lookupService,
                                              NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        InternalTransport transport = processTransportOnServiceAddition(lookupService, nioDetails);
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(lookupService, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(lookupService, jvmDetails);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(), lookupService,
                transport, operatingSystem, virtualMachine);

        ((InternalLookupServices) machine.getLookupServices()).addLookupService(lookupService);

        lookupServices.addLookupService(lookupService);
    }

    public synchronized void removeLookupService(String uid) {
        InternalLookupService lookupService = lookupServices.removeLookupService(uid);
        if (lookupService != null) {
            processTransportOnServiceRemoval(lookupService, lookupService);
            processOperatingSystemOnServiceRemoval(lookupService, lookupService);
            processVirtualMachineOnServiceRemoval(lookupService, lookupService);
            ((InternalLookupServices) ((InternalMachine) lookupService.getMachine()).getLookupServices()).removeLookupService(uid);
        }
    }

    public synchronized void addGridServiceManager(InternalGridServiceManager gridServiceManager,
                                                   NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        InternalTransport transport = processTransportOnServiceAddition(gridServiceManager, nioDetails);
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceManager, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceManager, jvmDetails);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(), gridServiceManager,
                transport, operatingSystem, virtualMachine);

        ((InternalGridServiceManagers) machine.getGridServiceManagers()).addGridServiceManager(gridServiceManager);

        gridServiceManagers.addGridServiceManager(gridServiceManager);
    }

    public synchronized void removeGridServiceManager(String uid) {
        InternalGridServiceManager gridServiceManager = gridServiceManagers.removeGridServiceManager(uid);
        if (gridServiceManager != null) {
            processTransportOnServiceRemoval(gridServiceManager, gridServiceManager);
            processOperatingSystemOnServiceRemoval(gridServiceManager, gridServiceManager);
            processVirtualMachineOnServiceRemoval(gridServiceManager, gridServiceManager);
            ((InternalGridServiceManagers) ((InternalMachine) gridServiceManager.getMachine()).getGridServiceManagers()).removeGridServiceManager(uid);
        }
    }

    public synchronized void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer,
                                                     NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        InternalTransport transport = processTransportOnServiceAddition(gridServiceContainer, nioDetails);
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceContainer, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceContainer, jvmDetails);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(), gridServiceContainer,
                transport, operatingSystem, virtualMachine);

        ((InternalGridServiceContainers) machine.getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);

        gridServiceContainers.addGridServiceContainer(gridServiceContainer);
    }

    public synchronized void removeGridServiceContainer(String uid) {
        InternalGridServiceContainer gridServiceContainer = gridServiceContainers.removeGridServiceContainer(uid);
        if (gridServiceContainer != null) {
            processTransportOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            processOperatingSystemOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            processVirtualMachineOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            ((InternalGridServiceContainers) ((InternalMachine) gridServiceContainer.getMachine()).getGridServiceContainers()).removeGridServiceContainer(uid);
        }
    }

    private InternalMachine processMachineOnServiceAddition(TransportDetails transportDetails, InternalMachineAware machineAware,
                                                            InternalTransport transport, OperatingSystem operatingSystem,
                                                            VirtualMachine virtualMachine) {
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(transportDetails.getHost());
        if (machine == null) {
            machine = new DefaultMachine(transportDetails.getHost(), transportDetails.getHost());
            machine.setOperatingSystem(operatingSystem);
            machines.addMachine(machine);
        }
        ((InternalTransports) machine.getTransports()).addTransport(transport);
        ((InternalVirtualMachines) machine.getVirtualMachines()).addVirtualMachine(virtualMachine);
        machineAware.setMachine(machine);
        return machine;
    }

    private InternalVirtualMachine processVirtualMachineOnServiceAddition(InternalVirtualMachineInfoProvider vmProvider, JVMDetails jvmDetails) {
        InternalVirtualMachine virtualMachine = (InternalVirtualMachine) virtualMachines.getVirtualMachineByUID(jvmDetails.getUid());
        if (virtualMachine == null) {
            virtualMachine = new DefaultVirtualMachine(jvmDetails);
            virtualMachines.addVirtualMachine(virtualMachine);
        }
        virtualMachine.addVirtualMachineInfoProvider(vmProvider);
        vmProvider.setVirtualMachine(virtualMachine);
        return virtualMachine;
    }

    private void processVirtualMachineOnServiceRemoval(InternalVirtualMachineInfoProvider vmProvider, InternalMachineAware machineAware) {
        InternalVirtualMachine virtualMachine = (InternalVirtualMachine) vmProvider.getVirtualMachine();
        virtualMachine.removeVirtualMachineInfoProvider(vmProvider);
        if (!virtualMachine.hasVirtualMachineInfoProviders()) {
            virtualMachines.removeVirtualMachine(virtualMachine.getUID());
            ((InternalVirtualMachines) machineAware.getMachine().getVirtualMachines()).removeVirtualMachine(virtualMachine.getUID());
        }
    }

    private InternalTransport processTransportOnServiceAddition(InternalTransportInfoProvider txProvider, NIODetails nioDetails) {
        InternalTransport transport = (InternalTransport) transports.getTransportByHostAndPort(nioDetails.getHost(), nioDetails.getPort());
        if (transport == null) {
            transport = new DefaultTransport(nioDetails);
            transports.addTransport(transport);
        }
        transport.addTransportInfoProvider(txProvider);
        txProvider.setTransport(transport);
        return transport;
    }

    private void processTransportOnServiceRemoval(InternalTransportInfoProvider txProvider, InternalMachineAware machineAware) {
        InternalTransport transport = ((InternalTransport) txProvider.getTransport());
        transport.removeTransportInfoProvider(txProvider);
        if (!transport.hasTransportInfoProviders()) {
            transports.removeTransport(transport.getUID());
            ((InternalTransports) machineAware.getMachine().getTransports()).removeTransport(transport.getUID());
        }
    }

    private InternalOperatingSystem processOperatingSystemOnServiceAddition(InternalOperatingSystemInfoProvider osProvider, OSDetails osDetails) {
        InternalOperatingSystem os = (InternalOperatingSystem) operatingSystems.getByUID(osDetails.getUID());
        if (os == null) {
            os = new DefaultOperatingSystem(osDetails);
            operatingSystems.addOperatingSystem(os);
        }
        os.addOperatingSystemInfoProvider(osProvider);
        osProvider.setOperatingSystem(os);
        return os;
    }

    private void processOperatingSystemOnServiceRemoval(InternalOperatingSystemInfoProvider osProvider, InternalMachineAware machineAware) {
        InternalOperatingSystem os = (InternalOperatingSystem) osProvider.getOperatingSystem();
        os.removeOperatingSystemInfoProvider(osProvider);
        if (!os.hasOperatingSystemInfoProviders()) {
            operatingSystems.removeOperatingSystem(os.getUID());
            ((InternalMachine) machineAware.getMachine()).setOperatingSystem(null);
        }
    }
}
