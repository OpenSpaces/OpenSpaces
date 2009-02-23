package org.openspaces.admin.internal.admin;

import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.grid.gsm.PUsDetails;
import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.operatingsystem.OSDetails;
import com.j_spaces.core.IJSpace;
import net.jini.core.discovery.LookupLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.discovery.DiscoveryService;
import org.openspaces.admin.internal.gsa.DefaultGridServiceAgents;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
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
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.DefaultProcessingUnits;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstance;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnits;
import org.openspaces.admin.internal.space.DefaultSpace;
import org.openspaces.admin.internal.space.DefaultSpaces;
import org.openspaces.admin.internal.space.InternalSpace;
import org.openspaces.admin.internal.space.InternalSpaceInstance;
import org.openspaces.admin.internal.space.InternalSpaces;
import org.openspaces.admin.internal.support.EventRegistrationHelper;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
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
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystem;
import org.openspaces.admin.os.OperatingSystems;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.Transports;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachines;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);

    private ScheduledThreadPoolExecutor scheduledExecutorService;

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices(this);

    private final InternalMachines machines = new DefaultMachines(this);

    private final InternalGridServiceAgents gridServiceAgents = new DefaultGridServiceAgents(this);

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers(this);

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers(this);

    private final InternalTransports transports = new DefaultTransports(this);

    private final InternalOperatingSystems operatingSystems = new DefaultOperatingSystems(this);

    private final InternalVirtualMachines virtualMachines = new DefaultVirtualMachines(this);

    private final InternalProcessingUnits processingUnits = new DefaultProcessingUnits(this);

    private final InternalProcessingUnitInstances processingUnitInstances = new DefaultProcessingUnitInstances(this);

    private final Map<String, InternalAgentGridComponent> orphanedAgentGridComponents = new ConcurrentHashMap<String, InternalAgentGridComponent>();

    private final InternalSpaces spaces = new DefaultSpaces(this);

    private ExecutorService[] eventsExecutorServices;

    private int eventsNumberOfThreads = 10;

    private LinkedList<Runnable>[] eventsQueue;

    private volatile long scheduledProcessingUnitMonitorInterval = 1000; // default to one second

    private volatile long scheduledAgentProcessessMonitorInterval = 5000; // defaults to 5 seconds

    private volatile long scheduledSpaceMonitorInterval = 1000; // default to one second

    private Future scheduledAgentProcessessMonitorFuture;

    private Future scheduledProcessingUnitMonitorFuture;

    private boolean scheduledStatisticsMonitor = false;

    private volatile boolean closed = false;

    private volatile String username;

    private volatile String password;

    public DefaultAdmin() {
        this.discoveryService = new DiscoveryService(this);
    }

    public String[] getGroups() {
        return discoveryService.getGroups();
    }

    public LookupLocator[] getLocators() {
        return discoveryService.getLocators();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addGroup(String group) {
        discoveryService.addGroup(group);
    }

    public void addLocator(String locator) {
        discoveryService.addLocator(locator);
    }

    public void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.spaces.setStatisticsInterval(interval, timeUnit);
        this.virtualMachines.setStatisticsInterval(interval, timeUnit);
        this.transports.setStatisticsInterval(interval, timeUnit);
        this.operatingSystems.setStatisticsInterval(interval, timeUnit);
        this.processingUnits.setStatisticsInterval(interval, timeUnit);
    }

    public synchronized void startStatisticsMonitor() {
        scheduledStatisticsMonitor = true;
        this.spaces.startStatisticsMonitor();
        this.virtualMachines.startStatisticsMonitor();
        this.transports.startStatisticsMonitor();
        this.operatingSystems.startStatisticsMonitor();
        this.processingUnits.startStatisticsMonitor();
    }

    public synchronized void stopStatisticsMontior() {
        scheduledStatisticsMonitor = false;
        this.spaces.stopStatisticsMontior();
        this.virtualMachines.stopStatisticsMontior();
        this.transports.stopStatisticsMontior();
        this.operatingSystems.stopStatisticsMontior();
        this.processingUnits.stopStatisticsMontior();
    }

    public boolean isMonitoring() {
        return scheduledStatisticsMonitor;
    }

    public void begin() {
        eventsExecutorServices = new ExecutorService[eventsNumberOfThreads];
        eventsQueue = new LinkedList[eventsNumberOfThreads];
        for (int i = 0; i < eventsNumberOfThreads; i++) {
            eventsExecutorServices[i] = Executors.newFixedThreadPool(1);
            eventsQueue[i] = new LinkedList<Runnable>();
        }

        discoveryService.start();
        this.scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
        scheduledProcessingUnitMonitorFuture = scheduledExecutorService.scheduleWithFixedDelay(
                new ScheduledProcessingUnitMonitor(), scheduledProcessingUnitMonitorInterval, scheduledProcessingUnitMonitorInterval, TimeUnit.MILLISECONDS);
        scheduledAgentProcessessMonitorFuture = scheduledExecutorService.scheduleWithFixedDelay(new ScheduledAgentProcessessMonitor(),
                scheduledAgentProcessessMonitorInterval, scheduledAgentProcessessMonitorInterval, TimeUnit.MILLISECONDS);
    }

    public void setProcessingUnitMonitorInterval(long interval, TimeUnit timeUnit) {
        if (closed) {
            throw new IllegalStateException("Admin already closed");
        }
        this.scheduledProcessingUnitMonitorInterval = timeUnit.toMillis(interval);
        if (scheduledProcessingUnitMonitorFuture != null) { // during initialization
            scheduledProcessingUnitMonitorFuture.cancel(false);
            scheduledProcessingUnitMonitorFuture = scheduledExecutorService.scheduleWithFixedDelay(new ScheduledProcessingUnitMonitor(), interval, interval, timeUnit);
        }
    }

    public void setAgentProcessessMonitorInterval(long interval, TimeUnit timeUnit) {
        if (closed) {
            throw new IllegalStateException("Admin already closed");
        }
        this.scheduledAgentProcessessMonitorInterval = timeUnit.toMillis(interval);
        if (scheduledAgentProcessessMonitorFuture != null) { // during initialization
            scheduledAgentProcessessMonitorFuture.cancel(false);
            scheduledAgentProcessessMonitorFuture = scheduledExecutorService.scheduleWithFixedDelay(new ScheduledAgentProcessessMonitor(), interval, interval, timeUnit);
        }
    }

    public long getScheduledSpaceMonitorInterval() {
        return scheduledSpaceMonitorInterval;
    }

    public synchronized void setSpaceMonitorInterval(long interval, TimeUnit timeUnit) {
        this.scheduledSpaceMonitorInterval = timeUnit.toMillis(interval);
        this.spaces.refreshScheduledSpaceMonitors();
    }

    public ScheduledThreadPoolExecutor getScheduler() {
        return this.scheduledExecutorService;
    }

    public void setSchedulerCorePoolSize(int coreThreads) {
        scheduledExecutorService.setCorePoolSize(coreThreads);
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        discoveryService.stop();
        scheduledExecutorService.shutdownNow();
        for (ExecutorService executorService : eventsExecutorServices) {
            executorService.shutdownNow();
        }
    }

    public LookupServices getLookupServices() {
        return this.lookupServices;
    }

    public GridServiceAgents getGridServiceAgents() {
        return this.gridServiceAgents;
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

    public OperatingSystems getOperatingSystems() {
        return operatingSystems;
    }

    public ProcessingUnits getProcessingUnits() {
        return this.processingUnits;
    }

    public Spaces getSpaces() {
        return this.spaces;
    }

    public void addEventListener(AdminEventListener eventListener) {
        EventRegistrationHelper.addEventListener(this, eventListener);
    }

    public void removeEventListener(AdminEventListener eventListener) {
        EventRegistrationHelper.removeEventListener(this, eventListener);
    }

    public synchronized void pushEvent(Object listener, Runnable notifier) {
        eventsQueue[Math.abs(listener.hashCode()) % eventsExecutorServices.length].add(new LoggerRunnable(notifier));
    }

    public synchronized void pushEventAsFirst(Object listener, Runnable notifier) {
        eventsQueue[Math.abs(listener.hashCode()) % eventsExecutorServices.length].addFirst(new LoggerRunnable(notifier));
    }

    public synchronized void flushEvents() {
        for (int i = 0; i < eventsNumberOfThreads; i++) {
            for (Runnable notifier : eventsQueue[i]) {
                eventsExecutorServices[i].submit(notifier);
            }
            eventsQueue[i].clear();
        }
    }

    public synchronized void raiseEvent(Object listener, Runnable notifier) {
        eventsExecutorServices[Math.abs(listener.hashCode()) % eventsExecutorServices.length].submit(new LoggerRunnable(notifier));
    }

    public synchronized void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceAgent, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceAgent, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceAgent, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceAgent);

        ((InternalGridServiceAgents) machine.getGridServiceAgents()).addGridServiceAgent(gridServiceAgent);
        ((InternalGridServiceAgents) ((InternalVirtualMachine) virtualMachine).getGridServiceAgents()).addGridServiceAgent(gridServiceAgent);

        gridServiceAgents.addGridServiceAgent(gridServiceAgent);

        for (Iterator<Map.Entry<String, InternalAgentGridComponent>> it = orphanedAgentGridComponents.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, InternalAgentGridComponent> entry = it.next();
            if (entry.getValue().getAgentUid().equals(gridServiceAgent.getUid())) {
                entry.getValue().setGridServiceAgent(gridServiceAgent);
                it.remove();
            }
        }

        flushEvents();
    }

    public synchronized void removeGridServiceAgent(String uid) {
        InternalGridServiceAgent gridServiceAgent = gridServiceAgents.removeGridServiceAgent(uid);
        if (gridServiceAgent != null) {
            processTransportOnServiceRemoval(gridServiceAgent, gridServiceAgent);
            processOperatingSystemOnServiceRemoval(gridServiceAgent, gridServiceAgent);
            processVirtualMachineOnServiceRemoval(gridServiceAgent, gridServiceAgent);
            processMachineOnServiceRemoval(gridServiceAgent);
            ((InternalGridServiceAgents) ((InternalMachine) gridServiceAgent.getMachine()).getGridServiceAgents()).removeGridServiceAgent(uid);

            for (Iterator<Map.Entry<String, InternalAgentGridComponent>> it = orphanedAgentGridComponents.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, InternalAgentGridComponent> entry = it.next();
                if (entry.getValue().getAgentUid().equals(gridServiceAgent.getUid())) {
                    it.remove();
                }
            }
        }

        flushEvents();
    }

    public synchronized void addLookupService(InternalLookupService lookupService,
                                              NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(lookupService, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(lookupService, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(lookupService, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, lookupService);

        ((InternalLookupServices) machine.getLookupServices()).addLookupService(lookupService);

        processAgentOnServiceAddition(lookupService);

        lookupServices.addLookupService(lookupService);

        flushEvents();
    }

    public synchronized void removeLookupService(String uid) {
        InternalLookupService lookupService = lookupServices.removeLookupService(uid);
        if (lookupService != null) {
            processTransportOnServiceRemoval(lookupService, lookupService);
            processOperatingSystemOnServiceRemoval(lookupService, lookupService);
            processVirtualMachineOnServiceRemoval(lookupService, lookupService);
            processMachineOnServiceRemoval(lookupService);
            ((InternalLookupServices) ((InternalMachine) lookupService.getMachine()).getLookupServices()).removeLookupService(uid);
        }

        flushEvents();
    }

    public synchronized void addGridServiceManager(InternalGridServiceManager gridServiceManager,
                                                   NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceManager, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceManager, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceManager, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceManager);

        ((InternalGridServiceManagers) machine.getGridServiceManagers()).addGridServiceManager(gridServiceManager);
        ((InternalGridServiceManagers) ((InternalVirtualMachine) virtualMachine).getGridServiceManagers()).addGridServiceManager(gridServiceManager);

        processAgentOnServiceAddition(gridServiceManager);

        gridServiceManagers.addGridServiceManager(gridServiceManager);

        flushEvents();
    }

    public synchronized void removeGridServiceManager(String uid) {
        InternalGridServiceManager gridServiceManager = gridServiceManagers.removeGridServiceManager(uid);
        if (gridServiceManager != null) {
            processTransportOnServiceRemoval(gridServiceManager, gridServiceManager);
            processOperatingSystemOnServiceRemoval(gridServiceManager, gridServiceManager);
            processVirtualMachineOnServiceRemoval(gridServiceManager, gridServiceManager);
            processMachineOnServiceRemoval(gridServiceManager);
            ((InternalGridServiceManagers) ((InternalMachine) gridServiceManager.getMachine()).getGridServiceManagers()).removeGridServiceManager(uid);
        }

        flushEvents();
    }

    public synchronized void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer,
                                                     NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceContainer, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceContainer, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceContainer, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceContainer);

        ((InternalGridServiceContainers) machine.getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);
        ((InternalGridServiceContainers) ((InternalVirtualMachine) virtualMachine).getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);

        processAgentOnServiceAddition(gridServiceContainer);
        
        gridServiceContainers.addGridServiceContainer(gridServiceContainer);

        flushEvents();
    }

    public synchronized void removeGridServiceContainer(String uid) {
        InternalGridServiceContainer gridServiceContainer = gridServiceContainers.removeGridServiceContainer(uid);
        if (gridServiceContainer != null) {
            processTransportOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            processOperatingSystemOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            processVirtualMachineOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            processMachineOnServiceRemoval(gridServiceContainer);
            ((InternalGridServiceContainers) ((InternalMachine) gridServiceContainer.getMachine()).getGridServiceContainers()).removeGridServiceContainer(uid);
        }

        flushEvents();
    }

    public synchronized void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(processingUnitInstance, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(processingUnitInstance, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(processingUnitInstance, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, processingUnitInstance);

        InternalProcessingUnit processingUnit = (InternalProcessingUnit) processingUnits.getProcessingUnit(processingUnitInstance.getClusterInfo().getName());
        if (processingUnit == null) {
            processingUnitInstances.addOrphaned(processingUnitInstance);
            return;
        }
        processProcessingUnitInstanceAddition(processingUnit, processingUnitInstance);

        flushEvents();
    }

    public synchronized void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeOrphaned(uid);
        InternalProcessingUnitInstance processingUnitInstance = (InternalProcessingUnitInstance) processingUnitInstances.removeInstnace(uid);
        if (processingUnitInstance != null) {
            ((InternalProcessingUnit) processingUnitInstance.getProcessingUnit()).removeProcessingUnitInstance(uid);
            ((InternalGridServiceContainer) processingUnitInstance.getGridServiceContainer()).removeProcessingUnitInstance(uid);
            ((InternalVirtualMachine) processingUnitInstance.getVirtualMachine()).removeProcessingUnitInstance(processingUnitInstance.getUid());
            ((InternalMachine) processingUnitInstance.getMachine()).removeProcessingUnitInstance(processingUnitInstance.getUid());

            processTransportOnServiceRemoval(processingUnitInstance, processingUnitInstance);
            processOperatingSystemOnServiceRemoval(processingUnitInstance, processingUnitInstance);
            processVirtualMachineOnServiceRemoval(processingUnitInstance, processingUnitInstance);
            processMachineOnServiceRemoval(processingUnitInstance);
        }

        flushEvents();
    }

    public synchronized void addSpaceInstance(InternalSpaceInstance spaceInstance, IJSpace clusteredIjspace, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(spaceInstance, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(spaceInstance, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(spaceInstance, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, spaceInstance);

        InternalSpace space = (InternalSpace) spaces.getSpaceByName(spaceInstance.getSpaceName());
        if (space == null) {
            space = new DefaultSpace(spaces, spaceInstance.getSpaceName(), spaceInstance.getSpaceName(), clusteredIjspace);
            spaces.addSpace(space);
        }
        spaceInstance.setSpace(space);
        space.addInstance(spaceInstance);
        spaces.addSpaceInstance(spaceInstance);

        // go over all the processing unit instnaces and add the space if matching
        for (ProcessingUnit processingUnit : processingUnits) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                ((InternalProcessingUnitInstance) processingUnitInstance).addSpaceInstnaceIfMatching(spaceInstance);
            }
        }

        machine.addSpaceInstance(spaceInstance);
        ((InternalVirtualMachine) virtualMachine).addSpaceInstance(spaceInstance);

        flushEvents();
    }

    public synchronized void removeSpaceInstance(String uid) {
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaces.removeSpaceInstance(uid);
        if (spaceInstance != null) {
            InternalSpace space = (InternalSpace) spaces.getSpaceByName(spaceInstance.getSpaceName());
            space.removeInstance(uid);
            if (space.getSize() == 0) {
                // no more instnaces, remove it completely
                spaces.removeSpace(space.getUid());
            }
            ((InternalVirtualMachine) spaceInstance.getVirtualMachine()).removeSpaceInstance(spaceInstance.getUid());
            ((InternalMachine) spaceInstance.getMachine()).removeSpaceInstance(spaceInstance.getUid());

            processTransportOnServiceRemoval(spaceInstance, spaceInstance);
            processOperatingSystemOnServiceRemoval(spaceInstance, spaceInstance);
            processVirtualMachineOnServiceRemoval(spaceInstance, spaceInstance);
            processMachineOnServiceRemoval(spaceInstance);
        }

        flushEvents();
    }

    private void processAgentOnServiceAddition(InternalAgentGridComponent agentGridComponent) {
        if (agentGridComponent.getAgentUid() == null) {
            // did not start by an agent, disard
            return;
        }
        GridServiceAgent gridServiceAgent = gridServiceAgents.getAgentByUID(agentGridComponent.getAgentUid());
        if (gridServiceAgent == null) {
            orphanedAgentGridComponents.put(agentGridComponent.getUid(), agentGridComponent);
        } else {
            agentGridComponent.setGridServiceAgent(gridServiceAgent);
        }
    }

    private synchronized void processProcessingUnitInstanceAddition(InternalProcessingUnit processingUnit, InternalProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.removeOrphaned(processingUnitInstance.getUid());

        processingUnitInstance.setProcessingUnit(processingUnit);
        processingUnit.addProcessingUnitInstance(processingUnitInstance);
        InternalGridServiceContainer gridServiceContainer = (InternalGridServiceContainer) gridServiceContainers.getContainerByUID(processingUnitInstance.getGridServiceContainerServiceID().toString());
        if (gridServiceContainer == null) {
            throw new IllegalStateException("Internal error in admin, should not happen");
        }
        processingUnitInstance.setGridServiceContainer(gridServiceContainer);
        gridServiceContainer.addProcessingUnitInstance(processingUnitInstance);

        ((InternalMachine) processingUnitInstance.getMachine()).addProcessingUnitInstance(processingUnitInstance);
        ((InternalVirtualMachine) processingUnitInstance.getVirtualMachine()).addProcessingUnitInstance(processingUnitInstance);

        // go over all the space instances, and add the matched one to the processing unit
        for (Space space : spaces) {
            for (SpaceInstance spaceInstance : space) {
                processingUnitInstance.addSpaceInstnaceIfMatching(spaceInstance);
            }
        }

        processingUnitInstances.addInstance(processingUnitInstance);
    }

    private InternalMachine processMachineOnServiceAddition(TransportDetails transportDetails,
                                                            InternalTransport transport, OperatingSystem operatingSystem,
                                                            VirtualMachine virtualMachine, InternalMachineAware... machineAwares) {
        InternalMachine machine = (InternalMachine) machines.getMachineByHostAddress(transportDetails.getLocalHostAddress());
        if (machine == null) {
            machine = new DefaultMachine(this, transportDetails.getLocalHostAddress(), transportDetails.getLocalHostAddress());
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

    private void processMachineOnServiceRemoval(InternalMachineAware machineAware) {
        Machine machine = machineAware.getMachine();
        machine = machines.getMachineByUID(machine.getUid());
        if (machine != null) {
            if (machine.getVirtualMachines().isEmpty()) {
                // no more virtual machines on the machine, we can remove it
                machines.removeMachine(machine);
            }
        }
    }

    private InternalVirtualMachine processVirtualMachineOnServiceAddition(InternalVirtualMachineInfoProvider vmProvider, JVMDetails jvmDetails) {
        InternalVirtualMachine virtualMachine = (InternalVirtualMachine) virtualMachines.getVirtualMachineByUID(jvmDetails.getUid());
        if (virtualMachine == null) {
            virtualMachine = new DefaultVirtualMachine(virtualMachines, jvmDetails);
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
            virtualMachines.removeVirtualMachine(virtualMachine.getUid());
            ((InternalVirtualMachines) machineAware.getMachine().getVirtualMachines()).removeVirtualMachine(virtualMachine.getUid());
        }
    }

    private InternalTransport processTransportOnServiceAddition(InternalTransportInfoProvider txProvider, NIODetails nioDetails, VirtualMachine virtualMachine) {
        InternalTransport transport = (InternalTransport) transports.getTransportByHostAndPort(nioDetails.getHost(), nioDetails.getPort());
        if (transport == null) {
            transport = new DefaultTransport(nioDetails, transports);
            transport.setVirtualMachine(virtualMachine);
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
            transports.removeTransport(transport.getUid());
            ((InternalTransports) machineAware.getMachine().getTransports()).removeTransport(transport.getUid());
        }
    }

    private InternalOperatingSystem processOperatingSystemOnServiceAddition(InternalOperatingSystemInfoProvider osProvider, OSDetails osDetails) {
        InternalOperatingSystem os = (InternalOperatingSystem) operatingSystems.getByUID(osDetails.getUID());
        if (os == null) {
            os = new DefaultOperatingSystem(osDetails, operatingSystems);
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
            operatingSystems.removeOperatingSystem(os.getUid());
            ((InternalMachine) machineAware.getMachine()).setOperatingSystem(null);
        }
    }

    private class ScheduledAgentProcessessMonitor implements Runnable {
        public void run() {
            for (GridServiceAgent gridServiceAgent : gridServiceAgents) {
                GSA gsa = ((InternalGridServiceAgent) gridServiceAgent).getGSA();
                try {
                    ((InternalGridServiceAgent) gridServiceAgent).setProcessesDetails(gsa.getDetails());
                } catch (Exception e) {
                    // failed to get the info, do nothing
                }
            }
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
                            holder.backupGSMs.put(gsm.getUid(), gsm);
                        }
                    }
                } catch (Exception e) {
                    if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                        // GSM is down, continue
                        continue;
                    }
                    logger.warn("Failed to get GSM details", e);
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
                    processingUnit = new DefaultProcessingUnit(DefaultAdmin.this, processingUnits, details);
                    newProcessingUnit = true;
                }
                // we always update the number of instances and backups since they might increate/decrease
                processingUnit.setNumberOfInstances(details.getNumberOfInstances());
                processingUnit.setNumberOfBackups(details.getNumberOfBackups());
                if (!newProcessingUnit) {
                    // handle managing GSM
                    if (holder.managingGSM == null) {
                        if (processingUnit.isManaged()) {
                            // event since we no longer have a managing GSM
                            processingUnit.setManagingGridServiceManager(null);
                        }
                    } else {
                        if (!processingUnit.isManaged() || !processingUnit.getManagingGridServiceManager().getUid().equals(holder.managingGSM.getUid())) {
                            // we changed managing GSM
                            processingUnit.setManagingGridServiceManager(holder.managingGSM);
                            // if it was in the backups, remove it from it
                            if (processingUnit.getBackupGridServiceManager(holder.managingGSM.getUid()) != null) {
                                processingUnit.removeBackupGridServiceManager(holder.managingGSM.getUid());
                            }
                        }
                    }
                    // handle backup GSM removal
                    for (GridServiceManager backupGSM : processingUnit.getBackupGridServiceManagers()) {
                        if (!holder.backupGSMs.containsKey(backupGSM.getUid())) {
                            processingUnit.removeBackupGridServiceManager(backupGSM.getUid());
                        }
                    }
                    // handle new backup GSMs
                    for (GridServiceManager backupGSM : holder.backupGSMs.values()) {
                        if (processingUnit.getBackupGridServiceManager(backupGSM.getUid()) == null) {
                            processingUnit.addBackupGridServiceManager(backupGSM);
                        }
                    }
                } else { // we have a new processing unit
                    processingUnits.addProcessingUnit(processingUnit);
                    processingUnit.setManagingGridServiceManager(holder.managingGSM);
                    for (GridServiceManager backupGSM : holder.backupGSMs.values()) {
                        processingUnit.addBackupGridServiceManager(backupGSM);
                    }
                }

                processingUnit.setStatus(details.getStatus());
            }

            // Now, process any orphaned processing unit instances
            synchronized (DefaultAdmin.this) {
                for (ProcessingUnitInstance orphaned : processingUnitInstances.getOrphaned()) {
                    InternalProcessingUnit processingUnit = (InternalProcessingUnit) processingUnits.getProcessingUnit(orphaned.getName());
                    if (processingUnit != null) {
                        processProcessingUnitInstanceAddition(processingUnit, (InternalProcessingUnitInstance) orphaned);
                    }
                }

                flushEvents();
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

    private static class LoggerRunnable implements Runnable {
        private final Runnable runnable;

        private LoggerRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public void run() {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.warn("Failed to executed event listener", e);
            }
        }
    }
}
