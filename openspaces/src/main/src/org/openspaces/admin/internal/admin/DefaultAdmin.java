package org.openspaces.admin.internal.admin;

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

import net.jini.core.discovery.LookupLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridComponent;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.discovery.DiscoveryService;
import org.openspaces.admin.internal.esm.DefaultElasticServiceManagers;
import org.openspaces.admin.internal.esm.InternalElasticServiceManager;
import org.openspaces.admin.internal.esm.InternalElasticServiceManagers;
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
import org.openspaces.admin.internal.zone.DefaultZone;
import org.openspaces.admin.internal.zone.DefaultZones;
import org.openspaces.admin.internal.zone.InternalZone;
import org.openspaces.admin.internal.zone.InternalZoneAware;
import org.openspaces.admin.internal.zone.InternalZones;
import org.openspaces.admin.lus.LookupService;
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
import org.openspaces.admin.zone.Zone;
import org.openspaces.admin.zone.ZoneAware;
import org.openspaces.admin.zone.Zones;

import com.gigaspaces.grid.gsa.GSA;
import com.gigaspaces.grid.gsm.PUDetails;
import com.gigaspaces.grid.gsm.PUsDetails;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.security.directory.UserDetails;
import org.openspaces.core.space.SpaceServiceDetails;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private static final Log logger = LogFactory.getLog(DefaultAdmin.class);

    private ScheduledThreadPoolExecutor scheduledExecutorService;

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices(this);

    private final InternalZones zones = new DefaultZones(this);

    private final InternalMachines machines = new DefaultMachines(this);

    private final InternalGridServiceAgents gridServiceAgents = new DefaultGridServiceAgents(this);

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers(this);
    
    private final InternalElasticServiceManagers elasticServiceManagers = new DefaultElasticServiceManagers(this);

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers(this);

    private final InternalTransports transports = new DefaultTransports(this);

    private final InternalOperatingSystems operatingSystems = new DefaultOperatingSystems(this);

    private final InternalVirtualMachines virtualMachines = new DefaultVirtualMachines(this);

    private final InternalProcessingUnits processingUnits = new DefaultProcessingUnits(this);

    private final InternalProcessingUnitInstances processingUnitInstances = new DefaultProcessingUnitInstances(this);

    private final Map<String, InternalAgentGridComponent> orphanedAgentGridComponents = new ConcurrentHashMap<String, InternalAgentGridComponent>();

    private final InternalSpaces spaces = new DefaultSpaces(this);

    private ExecutorService[] eventsExecutorServices;

    private final int eventsNumberOfThreads = 10;

    private LinkedList<Runnable>[] eventsQueue;

    private volatile long scheduledProcessingUnitMonitorInterval = 1000; // default to one second

    private volatile long scheduledAgentProcessessMonitorInterval = 5000; // defaults to 5 seconds

    private volatile long scheduledSpaceMonitorInterval = 1000; // default to one second

    private Future scheduledAgentProcessessMonitorFuture;

    private Future scheduledProcessingUnitMonitorFuture;

    private boolean scheduledStatisticsMonitor = false;

    private volatile boolean closed = false;

    private volatile UserDetails userDetails;

    private long defaultTimeout = Long.MAX_VALUE;

    private TimeUnit defaultTimeoutTimeUnit = TimeUnit.MILLISECONDS;

    public DefaultAdmin() {
        this.discoveryService = new DiscoveryService(this);
    }

    public String[] getGroups() {
        return discoveryService.getGroups();
    }

    public LookupLocator[] getLocators() {
        return discoveryService.getLocators();
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
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

    public void setStatisticsHistorySize(int historySize) {
        this.spaces.setStatisticsHistorySize(historySize);
        this.virtualMachines.setStatisticsHistorySize(historySize);
        this.transports.setStatisticsHistorySize(historySize);
        this.operatingSystems.setStatisticsHistorySize(historySize);
        this.processingUnits.setStatisticsHistorySize(historySize);
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
    
    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public TimeUnit getDefaultTimeoutTimeUnit() {
        return defaultTimeoutTimeUnit;
    }

    public DiscoveryService getDiscoveryService() {
        return this.discoveryService;
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
    
    public void setDefaultTimeout(long timeout, TimeUnit timeUnit) {
        this.defaultTimeout = timeout;
        this.defaultTimeoutTimeUnit = timeUnit;
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
    

    public ElasticServiceManagers getElasticServiceManagers() {
        return this.elasticServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return this.gridServiceContainers;
    }
    
    public GridComponent getGridComponentByUID(String uid) {
        GridComponent component = getGridServiceAgents().getAgentByUID(uid);
        if (component == null) {
            component = getGridServiceManagers().getManagerByUID(uid);
            if (component == null) {
                component = getGridServiceContainers().getContainerByUID(uid);
                if (component == null) {
                    component = getLookupServices().getLookupServiceByUID(uid);
                }
            }
        }
        return component;
    }

    public Machines getMachines() {
        return this.machines;
    }

    public Zones getZones() {
        return this.zones;
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
        eventsQueue[Math.abs(listener.hashCode() % eventsExecutorServices.length)].add(new LoggerRunnable(notifier));
    }

    public synchronized void pushEventAsFirst(Object listener, Runnable notifier) {
        eventsQueue[Math.abs(listener.hashCode() % eventsExecutorServices.length)].addFirst(new LoggerRunnable(notifier));
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
        eventsExecutorServices[Math.abs(listener.hashCode() % eventsExecutorServices.length)].submit(new LoggerRunnable(notifier));
    }

    public synchronized void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceAgent, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceAgent, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceAgent, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceAgent);

        processZonesOnServiceAddition(zones, gridServiceAgent.getUid(), transport, virtualMachine, machine, gridServiceAgent);

        ((InternalGridServiceAgents) machine.getGridServiceAgents()).addGridServiceAgent(gridServiceAgent);
        ((InternalGridServiceAgents) ((InternalVirtualMachine) virtualMachine).getGridServiceAgents()).addGridServiceAgent(gridServiceAgent);
        for (Zone zone : gridServiceAgent.getZones().values()) {
            ((InternalGridServiceAgents) zone.getGridServiceAgents()).addGridServiceAgent(gridServiceAgent);
        }

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
            gridServiceAgent.setDiscovered(false);
            processTransportOnServiceRemoval(gridServiceAgent, gridServiceAgent, gridServiceAgent);
            processOperatingSystemOnServiceRemoval(gridServiceAgent, gridServiceAgent);

            processVirtualMachineOnServiceRemoval(gridServiceAgent, gridServiceAgent, gridServiceAgent);
            ((InternalGridServiceAgents) ((InternalVirtualMachine) gridServiceAgent.getVirtualMachine()).getGridServiceAgents()).removeGridServiceAgent(uid);

            processMachineOnServiceRemoval(gridServiceAgent, gridServiceAgent);
            ((InternalGridServiceAgents) ((InternalMachine) gridServiceAgent.getMachine()).getGridServiceAgents()).removeGridServiceAgent(uid);

            processZonesOnServiceRemoval(uid, gridServiceAgent);
            for (Zone zone : gridServiceAgent.getZones().values()) {
                ((InternalGridServiceAgents) zone.getGridServiceAgents()).removeGridServiceAgent(uid);
            }

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
            NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(lookupService, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(lookupService, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(lookupService, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, lookupService);

        processZonesOnServiceAddition(zones, lookupService.getUid(), transport, virtualMachine, machine, lookupService);
        processAgentOnServiceAddition(lookupService);

        ((InternalLookupServices) machine.getLookupServices()).addLookupService(lookupService);

        for (Zone zone : lookupService.getZones().values()) {
            ((InternalLookupServices) zone.getLookupServices()).addLookupService(lookupService);
        }

        lookupServices.addLookupService(lookupService);

        flushEvents();
    }

    public synchronized void removeLookupService(String uid) {
        InternalLookupService lookupService = lookupServices.removeLookupService(uid);
        if (lookupService != null) {
            lookupService.setDiscovered(false);
            processTransportOnServiceRemoval(lookupService, lookupService, lookupService);
            processOperatingSystemOnServiceRemoval(lookupService, lookupService);
            processVirtualMachineOnServiceRemoval(lookupService, lookupService, lookupService);

            processMachineOnServiceRemoval(lookupService, lookupService);
            ((InternalLookupServices) ((InternalMachine) lookupService.getMachine()).getLookupServices()).removeLookupService(uid);

            processZonesOnServiceRemoval(uid, lookupService);
            for (Zone zone : lookupService.getZones().values()) {
                ((InternalLookupServices) zone.getLookupServices()).removeLookupService(uid);
            }
        }

        flushEvents();
    }

    public synchronized void addGridServiceManager(InternalGridServiceManager gridServiceManager,
            NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceManager, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceManager, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceManager, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceManager);

        processAgentOnServiceAddition(gridServiceManager);
        processZonesOnServiceAddition(zones, gridServiceManager.getUid(), transport, virtualMachine, machine, gridServiceManager);

        ((InternalGridServiceManagers) machine.getGridServiceManagers()).addGridServiceManager(gridServiceManager);
        ((InternalGridServiceManagers) ((InternalVirtualMachine) virtualMachine).getGridServiceManagers()).addGridServiceManager(gridServiceManager);
        for (Zone zone : gridServiceManager.getZones().values()) {
            ((InternalGridServiceManagers) zone.getGridServiceManagers()).addGridServiceManager(gridServiceManager);
        }

        gridServiceManagers.addGridServiceManager(gridServiceManager);

        flushEvents();
    }

    public synchronized void removeGridServiceManager(String uid) {
        InternalGridServiceManager gridServiceManager = gridServiceManagers.removeGridServiceManager(uid);
        if (gridServiceManager != null) {
            gridServiceManager.setDiscovered(false);
            processTransportOnServiceRemoval(gridServiceManager, gridServiceManager, gridServiceManager);
            processOperatingSystemOnServiceRemoval(gridServiceManager, gridServiceManager);

            processVirtualMachineOnServiceRemoval(gridServiceManager, gridServiceManager, gridServiceManager);
            ((InternalGridServiceManagers) ((InternalVirtualMachine) gridServiceManager.getVirtualMachine()).getGridServiceManagers()).removeGridServiceManager(uid);

            processMachineOnServiceRemoval(gridServiceManager, gridServiceManager);
            ((InternalGridServiceManagers) ((InternalMachine) gridServiceManager.getMachine()).getGridServiceManagers()).removeGridServiceManager(uid);

            processZonesOnServiceRemoval(uid, gridServiceManager);
            for (Zone zone : gridServiceManager.getZones().values()) {
                ((InternalGridServiceManagers) zone.getGridServiceManagers()).removeGridServiceManager(uid);
            }
        }

        flushEvents();
    }
    
    public synchronized void addElasticServiceManager(InternalElasticServiceManager elasticServiceManager,
            NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(elasticServiceManager, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(elasticServiceManager, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(elasticServiceManager, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, elasticServiceManager);

        processAgentOnServiceAddition(elasticServiceManager);
        processZonesOnServiceAddition(zones, elasticServiceManager.getUid(), transport, virtualMachine, machine, elasticServiceManager);

        ((InternalElasticServiceManagers) machine.getElasticServiceManagers()).addElasticServiceManager(elasticServiceManager);
        ((InternalElasticServiceManagers) ((InternalVirtualMachine) virtualMachine).getElasticServiceManagers()).addElasticServiceManager(elasticServiceManager);
        for (Zone zone : elasticServiceManager.getZones().values()) {
            ((InternalElasticServiceManagers) zone.getElasticServiceManagers()).addElasticServiceManager(elasticServiceManager);
        }

        elasticServiceManagers.addElasticServiceManager(elasticServiceManager);

        flushEvents();
    }

    public synchronized void removeElasticServiceManager(String uid) {
        InternalElasticServiceManager elasticServiceManager = elasticServiceManagers.removeElasticServiceManager(uid);
        if (elasticServiceManager != null) {
            elasticServiceManager.setDiscovered(false);
            processTransportOnServiceRemoval(elasticServiceManager, elasticServiceManager, elasticServiceManager);
            processOperatingSystemOnServiceRemoval(elasticServiceManager, elasticServiceManager);

            processVirtualMachineOnServiceRemoval(elasticServiceManager, elasticServiceManager, elasticServiceManager);
            ((InternalElasticServiceManagers) ((InternalVirtualMachine) elasticServiceManager.getVirtualMachine()).getElasticServiceManagers()).removeElasticServiceManager(uid);

            processMachineOnServiceRemoval(elasticServiceManager, elasticServiceManager);
            ((InternalElasticServiceManagers) ((InternalMachine) elasticServiceManager.getMachine()).getElasticServiceManagers()).removeElasticServiceManager(uid);

            processZonesOnServiceRemoval(uid, elasticServiceManager);
            for (Zone zone : elasticServiceManager.getZones().values()) {
                ((InternalElasticServiceManagers) zone.getElasticServiceManagers()).removeElasticServiceManager(uid);
            }
        }

        flushEvents();
    }

    public synchronized void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer,
            NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(gridServiceContainer, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(gridServiceContainer, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(gridServiceContainer, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, gridServiceContainer);

        processAgentOnServiceAddition(gridServiceContainer);

        processZonesOnServiceAddition(zones, gridServiceContainer.getUid(), transport, virtualMachine, machine, gridServiceContainer);

        ((InternalGridServiceContainers) machine.getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);
        ((InternalGridServiceContainers) ((InternalVirtualMachine) virtualMachine).getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);
        for (Zone zone : gridServiceContainer.getZones().values()) {
            ((InternalGridServiceContainers) zone.getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);
        }

        gridServiceContainers.addGridServiceContainer(gridServiceContainer);

        flushEvents();
    }

    public synchronized void removeGridServiceContainer(String uid) {
        InternalGridServiceContainer gridServiceContainer = gridServiceContainers.removeGridServiceContainer(uid);
        if (gridServiceContainer != null) {
            gridServiceContainer.setDiscovered(false);
            processTransportOnServiceRemoval(gridServiceContainer, gridServiceContainer, gridServiceContainer);
            processOperatingSystemOnServiceRemoval(gridServiceContainer, gridServiceContainer);

            processVirtualMachineOnServiceRemoval(gridServiceContainer, gridServiceContainer, gridServiceContainer);
            ((InternalGridServiceContainers) ((InternalVirtualMachine) gridServiceContainer.getVirtualMachine()).getGridServiceContainers()).removeGridServiceContainer(uid);

            processMachineOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            ((InternalGridServiceContainers) ((InternalMachine) gridServiceContainer.getMachine()).getGridServiceContainers()).removeGridServiceContainer(uid);

            processZonesOnServiceRemoval(uid, gridServiceContainer);
            for (Zone zone : gridServiceContainer.getZones().values()) {
                ((InternalGridServiceContainers) zone.getGridServiceContainers()).removeGridServiceContainer(uid);
            }
        }

        flushEvents();
    }

    public synchronized void addProcessingUnitInstance(InternalProcessingUnitInstance processingUnitInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(processingUnitInstance, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(processingUnitInstance, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(processingUnitInstance, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, processingUnitInstance);

        processZonesOnServiceAddition(zones, processingUnitInstance.getUid(), transport, virtualMachine, machine, processingUnitInstance);

        InternalProcessingUnit processingUnit = (InternalProcessingUnit) processingUnits.getProcessingUnit(processingUnitInstance.getClusterInfo().getName());
        InternalGridServiceContainer gridServiceContainer = (InternalGridServiceContainer) gridServiceContainers.getContainerByUID(processingUnitInstance.getGridServiceContainerServiceID().toString());

        if (processingUnit == null || gridServiceContainer == null) {
            processingUnitInstances.addOrphaned(processingUnitInstance);
        } else {
            processProcessingUnitInstanceAddition(processingUnit, processingUnitInstance);
        }

        flushEvents();
    }

    public synchronized void removeProcessingUnitInstance(String uid, boolean removeEmbeddedSpaces) {
        processingUnitInstances.removeOrphaned(uid);
        InternalProcessingUnitInstance processingUnitInstance = (InternalProcessingUnitInstance) processingUnitInstances.removeInstance(uid);
        if (processingUnitInstance != null) {
            processingUnitInstance.setDiscovered(false);
            ((InternalProcessingUnit) processingUnitInstance.getProcessingUnit()).removeProcessingUnitInstance(uid);
            ((InternalGridServiceContainer) processingUnitInstance.getGridServiceContainer()).removeProcessingUnitInstance(uid);
            ((InternalVirtualMachine) processingUnitInstance.getVirtualMachine()).removeProcessingUnitInstance(processingUnitInstance.getUid());
            ((InternalMachine) processingUnitInstance.getMachine()).removeProcessingUnitInstance(processingUnitInstance.getUid());
            for (Zone zone : processingUnitInstance.getZones().values()) {
                ((InternalZone) zone).removeProcessingUnitInstance(processingUnitInstance.getUid());
            }

            processTransportOnServiceRemoval(processingUnitInstance, processingUnitInstance, processingUnitInstance);
            processOperatingSystemOnServiceRemoval(processingUnitInstance, processingUnitInstance);
            processVirtualMachineOnServiceRemoval(processingUnitInstance, processingUnitInstance, processingUnitInstance);
            processMachineOnServiceRemoval(processingUnitInstance, processingUnitInstance);
            processZonesOnServiceRemoval(processingUnitInstance.getUid(), processingUnitInstance);

            if (removeEmbeddedSpaces) {
                for (SpaceServiceDetails serviceDetails : processingUnitInstance.getEmbeddedSpacesDetails()) {
                    removeSpaceInstance(serviceDetails.getServiceID().toString());
                }
            }
        }

        flushEvents();
    }

    public synchronized void addSpaceInstance(InternalSpaceInstance spaceInstance, NIODetails nioDetails, OSDetails osDetails, JVMDetails jvmDetails, String[] zones) {
        OperatingSystem operatingSystem = processOperatingSystemOnServiceAddition(spaceInstance, osDetails);
        VirtualMachine virtualMachine = processVirtualMachineOnServiceAddition(spaceInstance, jvmDetails);
        InternalTransport transport = processTransportOnServiceAddition(spaceInstance, nioDetails, virtualMachine);

        InternalMachine machine = processMachineOnServiceAddition(transport.getDetails(),
                transport, operatingSystem, virtualMachine,
                (InternalMachineAware) virtualMachine, spaceInstance);

        processZonesOnServiceAddition(zones, spaceInstance.getUid(), transport, virtualMachine, machine, spaceInstance);

        InternalSpace space = (InternalSpace) spaces.getSpaceByName(spaceInstance.getSpaceName());
        if (space == null) {
            space = new DefaultSpace(spaces, spaceInstance.getSpaceName(), spaceInstance.getSpaceName());
            spaces.addSpace(space);
        }
        spaceInstance.setSpace(space);
        space.addInstance(spaceInstance);
        spaces.addSpaceInstance(spaceInstance);

        // go over all the processing unit instances and add the space if matching
        for (ProcessingUnit processingUnit : processingUnits) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                ((InternalProcessingUnitInstance) processingUnitInstance).addSpaceInstanceIfMatching(spaceInstance);
            }
        }

        machine.addSpaceInstance(spaceInstance);
        ((InternalVirtualMachine) virtualMachine).addSpaceInstance(spaceInstance);
        for (Zone zone : spaceInstance.getZones().values()) {
            ((InternalZone) zone).addSpaceInstance(spaceInstance);
        }

        flushEvents();
    }

    public synchronized void removeSpaceInstance(String uid) {
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaces.removeSpaceInstance(uid);
        if (spaceInstance != null) {
            spaceInstance.setDiscovered(false);
            InternalSpace space = (InternalSpace) spaces.getSpaceByName(spaceInstance.getSpaceName());
            space.removeInstance(uid);
            if (space.getSize() == 0) {
                // no more instances, remove it completely
                spaces.removeSpace(space.getUid());
            }
            ((InternalVirtualMachine) spaceInstance.getVirtualMachine()).removeSpaceInstance(spaceInstance.getUid());
            ((InternalMachine) spaceInstance.getMachine()).removeSpaceInstance(spaceInstance.getUid());
            for (Zone zone : spaceInstance.getZones().values()) {
                ((InternalZone) zone).removeSpaceInstance(uid);
            }

            processTransportOnServiceRemoval(spaceInstance, spaceInstance, spaceInstance);
            processOperatingSystemOnServiceRemoval(spaceInstance, spaceInstance);
            processVirtualMachineOnServiceRemoval(spaceInstance, spaceInstance, spaceInstance);
            processMachineOnServiceRemoval(spaceInstance, spaceInstance);
            processZonesOnServiceRemoval(uid, spaceInstance);
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
        for (Zone zone : processingUnitInstance.getZones().values()) {
            ((InternalZone) zone).addProcessingUnitInstance(processingUnitInstance);
        }

        // go over all the space instances, and add the matched one to the processing unit
        for (Space space : spaces) {
            for (SpaceInstance spaceInstance : space) {
                processingUnitInstance.addSpaceInstanceIfMatching(spaceInstance);
            }
        }

        processingUnitInstances.addInstance(processingUnitInstance);
    }

    private void processZonesOnServiceAddition(String[] zonesNames, String zoneUidProvider,
            InternalTransport transport, VirtualMachine virtualMachine, Machine machine,
            InternalZoneAware... zoneAwares) {
        if (zonesNames == null) {
            return;
        }
        for (String zoneName : zonesNames) {
            InternalZone zone = (InternalZone) zones.getByName(zoneName);
            if (zone == null) {
                zone = new DefaultZone(this, zoneName);
            }
            zones.addZone(zone, zoneUidProvider);
            ((InternalTransports) zone.getTransports()).addTransport(transport);
            ((InternalVirtualMachines) zone.getVirtualMachines()).addVirtualMachine(virtualMachine);
            ((InternalMachines) zone.getMachines()).addMachine((InternalMachine) machine);
            for (InternalZoneAware zoneAware : zoneAwares) {
                zoneAware.addZone(zone);
            }
        }
    }

    private void processZonesOnServiceRemoval(String zoneUidProvider, ZoneAware zoneAware) {
        for (Zone zone : zoneAware.getZones().values()) {
            zones.removeProvider(zone, zoneUidProvider);
        }
    }

    private InternalMachine processMachineOnServiceAddition(TransportDetails transportDetails,
            InternalTransport transport, OperatingSystem operatingSystem,
            VirtualMachine virtualMachine, InternalMachineAware... machineAwares) {
        InternalMachine machine = (InternalMachine) machines.getMachineByHostAddress(transportDetails.getHostAddress());
        if (machine == null) {
            machine = new DefaultMachine(this, transportDetails.getHostAddress(), transportDetails.getHostAddress());
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

    private void processMachineOnServiceRemoval(InternalMachineAware machineAware, ZoneAware zoneAware) {
        Machine machine = machineAware.getMachine();
        machine = machines.getMachineByUID(machine.getUid());
        if (machine != null) {
            machines.removeMachine(machine);
            for (Zone zone : zoneAware.getZones().values()) {
                ((InternalMachines) zone.getMachines()).removeMachine(machine);
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

    private void processVirtualMachineOnServiceRemoval(InternalVirtualMachineInfoProvider vmProvider, InternalMachineAware machineAware, ZoneAware zoneAware) {
        InternalVirtualMachine virtualMachine = (InternalVirtualMachine) vmProvider.getVirtualMachine();
        virtualMachine.removeVirtualMachineInfoProvider(vmProvider);
        if (!virtualMachine.hasVirtualMachineInfoProviders()) {
            virtualMachines.removeVirtualMachine(virtualMachine.getUid());
            ((InternalVirtualMachines) machineAware.getMachine().getVirtualMachines()).removeVirtualMachine(virtualMachine.getUid());
            for (Zone zone : zoneAware.getZones().values()) {
                ((InternalVirtualMachines) zone.getVirtualMachines()).removeVirtualMachine(virtualMachine.getUid());
            }
        }
    }

    private InternalTransport processTransportOnServiceAddition(InternalTransportInfoProvider txProvider, NIODetails nioDetails, VirtualMachine virtualMachine) {
        InternalTransport transport = (InternalTransport) transports.getTransportByHostAndPort(nioDetails.getBindHost(), nioDetails.getPort());
        if (transport == null) {
            transport = new DefaultTransport(nioDetails, transports);
            transport.setVirtualMachine(virtualMachine);
            transports.addTransport(transport);
        }
        transport.addTransportInfoProvider(txProvider);
        txProvider.setTransport(transport);
        return transport;
    }

    private void processTransportOnServiceRemoval(InternalTransportInfoProvider txProvider, InternalMachineAware machineAware, ZoneAware zoneAware) {
        InternalTransport transport = ((InternalTransport) txProvider.getTransport());
        transport.removeTransportInfoProvider(txProvider);
        if (!transport.hasTransportInfoProviders()) {
            transports.removeTransport(transport.getUid());
            ((InternalTransports) machineAware.getMachine().getTransports()).removeTransport(transport.getUid());
            for (Zone zone : zoneAware.getZones().values()) {
                ((InternalTransports) zone.getTransports()).removeTransport(transport.getUid());
            }
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
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (GridServiceManager gsm : gridServiceManagers) {
            dumpResult.add(gsm.generateDump(cause, context, processor));
        }
        for (GridServiceContainer gsc : gridServiceContainers) {
            dumpResult.add(gsc.generateDump(cause, context, processor));
        }
        for (GridServiceAgent gsa : gridServiceAgents) {
            dumpResult.add(gsa.generateDump(cause, context, processor));
        }
        for (LookupService lus : lookupServices) {
            dumpResult.add(lus.generateDump(cause, context, processor));
        }
        return dumpResult;
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
                for (ProcessingUnitInstance orphanedX : processingUnitInstances.getOrphaned()) {
                    InternalProcessingUnitInstance orphaned = (InternalProcessingUnitInstance) orphanedX;

                    InternalProcessingUnit processingUnit = (InternalProcessingUnit) processingUnits.getProcessingUnit(orphaned.getName());
                    InternalGridServiceContainer gridServiceContainer = (InternalGridServiceContainer) gridServiceContainers.getContainerByUID(orphaned.getGridServiceContainerServiceID().toString());

                    if (processingUnit != null && gridServiceContainer != null) {
                        processProcessingUnitInstanceAddition(processingUnit, orphaned);
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
