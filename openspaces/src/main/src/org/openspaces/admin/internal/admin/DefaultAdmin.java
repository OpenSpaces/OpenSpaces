package org.openspaces.admin.internal.admin;

import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.grid.gsm.PUsDetails;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
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
import org.openspaces.admin.internal.pu.DefaultProcessingUnit;
import org.openspaces.admin.internal.pu.DefaultProcessingUnits;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
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
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachines;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private final ScheduledExecutorService scheduledExecutorService;

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalMachines machines = new DefaultMachines();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    private final InternalTransports transports = new DefaultTransports();

    private final InternalOperatingSystems operatingSystems = new DefaultOperatingSystems();

    private final InternalVirtualMachines virtualMachines = new DefaultVirtualMachines();

    private final InternalProcessingUnits processingUnits = new DefaultProcessingUnits();

    public DefaultAdmin(String[] groups, LookupLocator[] locators) {
        this.discoveryService = new DiscoveryService(groups, locators, this);
        discoveryService.start();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
        scheduledExecutorService.scheduleWithFixedDelay(new ScheduledProcessingUnitMonitor(), 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void close() {
        discoveryService.stop();
        scheduledExecutorService.shutdownNow();
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

    public ProcessingUnits getProcessingUnits() {
        return this.processingUnits;
    }

    public synchronized void addLookupService(InternalLookupService lookupService,
                                              NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        InternalTransport transport = processTransportOnServiceAddition(lookupService, nioDetails);
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(lookupService, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(lookupService, jvmDetails);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, lookupService);

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

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceManager);

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

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceContainer);

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

    private InternalMachine processMachineOnServiceAddition(TransportDetails transportDetails,
                                                            InternalTransport transport, OperatingSystem operatingSystem,
                                                            VirtualMachine virtualMachine, InternalMachineAware ... machineAwares) {
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(transportDetails.getLocalHostAddress());
        if (machine == null) {
            machine = new DefaultMachine(transportDetails.getLocalHostAddress(), transportDetails.getLocalHostAddress());
            machine.setOperatingSystem(operatingSystem);
            machines.addMachine(machine);
        }
        ((InternalTransports) machine.getTransports()).addTransport(transport);
        ((InternalVirtualMachines) machine.getVirtualMachines()).addVirtualMachine(virtualMachine);
        for (InternalMachineAware machineAware : machineAwares) {
            machineAware.setMachine(machine);
        }
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

    private class ScheduledProcessingUnitMonitor implements Runnable {

        public void run() {
            Map<String, Holder> holders = new HashMap<String, Holder>();
            for (GridServiceManager gsm : gridServiceManagers) {
                try {
                    PUsDetails pusDetails = ((InternalGridServiceManager) gsm).getGSM().getPUsDetails();
                    for (PUDetails detail : pusDetails.getDetails()) {
                        Holder holder = holders.get(detail.getName()); 
                        if (holder == null) {
                            holder = new Holder();
                            holder.name = detail.getName();
                            holders.put(holder.name, holder);
                        }
                        if (detail.isManaging()) {
                            holder.detail = detail;
                            holder.managingGSM = gsm;
                        } else {
                            holder.backupDetail = detail;
                            holder.backupGSMs.put(gsm.getUID(), gsm);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // first go over all of them and remove the ones needed
            for (ProcessingUnit processingUnit : processingUnits) {
                if (!holders.containsKey(processingUnit.getName())) {
                    processingUnits.removeProcessingUnit(processingUnit.getName());
                }
            }
            // now, go over and update what needed to be updated
            for (Holder holder : holders.values()) {
                PUDetails details = holder.detail;
                if (details == null) {
                    details = holder.backupDetail;
                }
                boolean newProcessingUnit = false;
                InternalProcessingUnit processingUnit = (InternalProcessingUnit) processingUnits.getProcessingUnit(holder.name);
                if (processingUnit == null) {
                    processingUnit = new DefaultProcessingUnit(details);
                    newProcessingUnit = true;
                } else {
                    boolean changed = processingUnit.setStatus(details.getStatus());
                    // changed for future events
                }
                if (!newProcessingUnit) {
                    // handle managing GSM
                    if (holder.managingGSM == null) {
                        if (processingUnit.isManaged()) {
                            // event since we no longer have a managing GSM
                            processingUnit.setManagingGridServiceManager(null);
                        }
                    } else {
                        if (!processingUnit.isManaged() || !processingUnit.getManagingGridServiceManager().getUID().equals(holder.managingGSM.getUID())) {
                            // we changed managing GSM
                            processingUnit.setManagingGridServiceManager(holder.managingGSM);
                            // if it was in the backups, remove it from it
                            if (processingUnit.getBackupGridServiceManager(holder.managingGSM.getUID()) != null) {
                                processingUnit.removeBackupGridServiceManager(holder.managingGSM.getUID());
                            }
                        }
                    }
                    // handle backup GSM removal
                    for (GridServiceManager backupGSM : processingUnit.getBackupGridServiceManagers()) {
                        if (!holder.backupGSMs.containsKey(backupGSM.getUID())) {
                            processingUnit.removeBackupGridServiceManager(backupGSM.getUID());
                        }
                    }
                    // handle new backup GSMs
                    for (GridServiceManager backupGSM : holder.backupGSMs.values()) {
                        if (processingUnit.getBackupGridServiceManager(backupGSM.getUID()) == null) {
                            processingUnit.addBackupGridServiceManager(backupGSM);
                        }
                    }
                } else { // we have a new processing unit
                    processingUnit.setManagingGridServiceManager(holder.managingGSM);
                    for (GridServiceManager backupGSM : holder.backupGSMs.values()) {
                        processingUnit.addBackupGridServiceManager(backupGSM);
                    }
                    processingUnits.addProcessingUnit(processingUnit);
                }
            }
        }

        private class Holder {

            String name;

            PUDetails detail;

            PUDetails backupDetail;

            GridServiceManager managingGSM;

            Map<String, GridServiceManager> backupGSMs = new HashMap<String, GridServiceManager>();
        }
    }
}
