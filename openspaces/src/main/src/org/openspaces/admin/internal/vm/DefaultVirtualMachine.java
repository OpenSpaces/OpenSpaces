package org.openspaces.admin.internal.vm;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.esm.ElasticServiceManager;
import org.openspaces.admin.esm.ElasticServiceManagers;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.DefaultElasticServiceManagers;
import org.openspaces.admin.internal.esm.InternalElasticServiceManagers;
import org.openspaces.admin.internal.gsa.DefaultGridServiceAgents;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgents;
import org.openspaces.admin.internal.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.lus.DefaultLookupServices;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.space.DefaultSpaceInstances;
import org.openspaces.admin.internal.space.InternalSpaceInstances;
import org.openspaces.admin.internal.vm.events.DefaultVirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.internal.vm.events.InternalVirtualMachineStatisticsChangedEventManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.LookupServices;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEvent;
import org.openspaces.admin.vm.events.VirtualMachineStatisticsChangedEventManager;
import org.openspaces.core.util.ConcurrentHashSet;

import com.gigaspaces.internal.jvm.JVMDetails;

/**
 * @author kimchy
 */
public class DefaultVirtualMachine implements InternalVirtualMachine {

    private final InternalAdmin admin;

    private final InternalVirtualMachines virtualMachines;

    private final String uid;

    private final VirtualMachineDetails details;

    private final Set<InternalVirtualMachineInfoProvider> virtualMachineInfoProviders = new ConcurrentHashSet<InternalVirtualMachineInfoProvider>();

    private volatile Machine machine;
    
    private final InternalLookupServices lookupServices;
   
    private final InternalGridServiceAgents gridServiceAgents;

    private final InternalGridServiceManagers gridServiceManagers;
    
    private final InternalElasticServiceManagers elasticServiceManagers;

    private final InternalGridServiceContainers gridServiceContainers;

    private final InternalProcessingUnitInstances processingUnitInstances;

    private final InternalSpaceInstances spaceInstances;

    private long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private VirtualMachineStatistics lastStatistics;

    private Future scheduledStatisticsMonitor;
    
    private int scheduledStatisticsRefCount = 0;

    private final InternalVirtualMachineStatisticsChangedEventManager statisticsChangedEventManager;

    public DefaultVirtualMachine(InternalVirtualMachines virtualMachines, JVMDetails details, String jmxUrl) {
        this.virtualMachines = virtualMachines;
        this.admin = (InternalAdmin) virtualMachines.getAdmin();
        this.details = new DefaultVirtualMachineDetails(details,jmxUrl);
        this.uid = details.getUid();
        this.lookupServices = new DefaultLookupServices(admin);
        this.gridServiceAgents = new DefaultGridServiceAgents(admin);
        this.gridServiceManagers = new DefaultGridServiceManagers(admin);
        this.elasticServiceManagers = new DefaultElasticServiceManagers(admin);
        this.gridServiceContainers = new DefaultGridServiceContainers(admin);
        this.processingUnitInstances = new DefaultProcessingUnitInstances(admin);
        this.spaceInstances = new DefaultSpaceInstances(admin);
        this.statisticsChangedEventManager = new DefaultVirtualMachineStatisticsChangedEventManager(admin, this);
    }

    public String getUid() {
        return this.uid;
    }

    public void addVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        assertStateChangesPermitted();
        virtualMachineInfoProviders.add(virtualMachineInfoProvider);
    }

    public void removeVirtualMachineInfoProvider(InternalVirtualMachineInfoProvider virtualMachineInfoProvider) {
        assertStateChangesPermitted();
        virtualMachineInfoProviders.remove(virtualMachineInfoProvider);
    }

    public boolean hasVirtualMachineInfoProviders() {
        return !virtualMachineInfoProviders.isEmpty();
    }

    public VirtualMachineStatisticsChangedEventManager getVirtualMachineStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        assertStateChangesPermitted();
        this.machine = machine;
    }
    
    public LookupService getLookupService() {
        Iterator<LookupService> it = lookupServices.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }
    
    public LookupServices getLookupServices() {
        return this.lookupServices;
    }

    public GridServiceAgents getGridServiceAgents() {
        return this.gridServiceAgents;
    }

    public GridServiceAgent getGridServiceAgent() {
        Iterator<GridServiceAgent> it = gridServiceAgents.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public GridServiceManager getGridServiceManager() {
        Iterator<GridServiceManager> it = gridServiceManagers.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }
    
    public ElasticServiceManager getElasticServiceManager() {
        Iterator<ElasticServiceManager> it = elasticServiceManagers.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public GridServiceContainer getGridServiceContainer() {
        Iterator<GridServiceContainer> it = gridServiceContainers.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public GridServiceManagers getGridServiceManagers() {
        return gridServiceManagers;
    }
    
    public ElasticServiceManagers getElasticServiceManagers() {
        return elasticServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return gridServiceContainers;
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public SpaceInstance[] getSpaceInstances() {
        return spaceInstances.getSpaceInstances();
    }

    public SpaceInstanceAddedEventManager getSpaceInstanceAdded() {
        return spaceInstances.getSpaceInstanceAdded();
    }

    public SpaceInstanceRemovedEventManager getSpaceInstanceRemoved() {
        return spaceInstances.getSpaceInstanceRemoved();
    }

    public void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.addLifecycleListener(eventListener);
    }

    public void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstances.removeLifecycleListener(eventListener);
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        processingUnitInstances.removeInstance(uid);
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.addSpaceInstance(spaceInstance);
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.removeSpaceInstance(uid);
    }

    public void runGc() {
        for (InternalVirtualMachineInfoProvider provider : virtualMachineInfoProviders) {
            try {
                provider.runGc();
                break;
            } catch (RemoteException e) {
                // continue to the next one
            }
        }
    }

    private static final VirtualMachineStatistics NA_STATS = new DefaultVirtualMachineStatistics();

    public synchronized VirtualMachineStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        VirtualMachineStatistics previousStatistics = lastStatistics;
        lastStatistics = NA_STATS;
        lastStatisticsTimestamp = currentTime;
        for (InternalVirtualMachineInfoProvider provider : virtualMachineInfoProviders) {
            try {
                lastStatistics = new DefaultVirtualMachineStatistics(provider.getJVMStatistics(), previousStatistics, getDetails(), statisticsHistorySize, getMachine().getOperatingSystem().getTimeDelta());
                break;
            } catch (RemoteException e) {
                // continue to the next one
            }
        }
        return lastStatistics;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        this.statisticsInterval = timeUnit.toMillis(interval);
        if (scheduledStatisticsMonitor != null) {
            stopStatisticsMonitor();
            startStatisticsMonitor();
        }
    }

    public synchronized void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
    }

    public synchronized void startStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        final VirtualMachine virtualMachine = this;
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                VirtualMachineStatistics stats = virtualMachine.getStatistics();
                VirtualMachineStatisticsChangedEvent event = new VirtualMachineStatisticsChangedEvent(virtualMachine, stats);
                statisticsChangedEventManager.virtualMachineStatisticsChanged(event);
                ((InternalVirtualMachineStatisticsChangedEventManager) virtualMachines.getVirtualMachineStatisticsChanged()).virtualMachineStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultVirtualMachine that = (DefaultVirtualMachine) o;
        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }
}
