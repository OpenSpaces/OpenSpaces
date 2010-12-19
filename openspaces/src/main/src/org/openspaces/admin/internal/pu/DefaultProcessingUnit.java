package org.openspaces.admin.internal.pu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.esm.ProcessingUnitElasticConfig;
import org.openspaces.admin.internal.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.pu.events.DefaultBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.internal.pu.events.DefaultProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalBackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.internal.pu.events.InternalProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.space.Space;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.requirement.Requirement;
import org.openspaces.pu.sla.requirement.ZoneRequirement;

import com.gigaspaces.grid.gsm.PUDetails;

/**
 * @author kimchy
 */
public class DefaultProcessingUnit implements InternalProcessingUnit {

    private final InternalProcessingUnits processingUnits;

    private final InternalAdmin admin;

    private final String name;

    private volatile int numberOfInstances;

    private volatile int numberOfBackups;

    private final BeanLevelProperties beanLevelProperties;

    private final SLA sla;

    private volatile DeploymentStatus deploymentStatus = DeploymentStatus.NA;

    private volatile GridServiceManager managingGridServiceManager;

    private final Map<String, GridServiceManager> backupGridServiceManagers = new ConcurrentHashMap<String, GridServiceManager>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<Integer, ProcessingUnitPartition> processingUnitPartitions = new ConcurrentHashMap<Integer, ProcessingUnitPartition>();

    private final ConcurrentMap<String, Space> spaces = new ConcurrentHashMap<String, Space>();

    private final InternalManagingGridServiceManagerChangedEventManager managingGridServiceManagerChangedEventManager;

    private final InternalBackupGridServiceManagerChangedEventManager backupGridServiceManagerChangedEventManager;

    private final InternalProcessingUnitStatusChangedEventManager processingUnitStatusChangedEventManager;

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    private final InternalProcessingUnitSpaceCorrelatedEventManager spaceCorrelatedEventManager;


    private final InternalProcessingUnitInstanceStatisticsChangedEventManager processingUnitInstanceStatisticsChangedEventManager;

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private volatile boolean scheduledStatisticsMonitor = false;

    public DefaultProcessingUnit(InternalAdmin admin, InternalProcessingUnits processingUnits, PUDetails details) {
        this.admin = admin;
        this.processingUnits = processingUnits;
        this.name = details.getName();
        this.numberOfInstances = details.getNumberOfInstances();
        this.numberOfBackups = details.getNumberOfBackups();
        try {
            this.beanLevelProperties = (BeanLevelProperties) details.getBeanLevelProperties().get();
        } catch (Exception e) {
            throw new AdminException("Failed to get bean level properties", e);
        }

        try {
            this.sla = (SLA) details.getSla().get();
        } catch (Exception e) {
            throw new AdminException("Failed to get sla", e);
        }

        if (numberOfBackups == 0) {
            // if we have no backup, its actually just a "single partition"
            processingUnitPartitions.put(0, new DefaultProcessingUnitPartition(this, 0));
        } else {
            for (int i = 0; i < numberOfInstances; i++) {
                processingUnitPartitions.put(i, new DefaultProcessingUnitPartition(this, i));
            }
        }

        this.managingGridServiceManagerChangedEventManager = new DefaultManagingGridServiceManagerChangedEventManager(admin);
        this.backupGridServiceManagerChangedEventManager = new DefaultBackupGridServiceManagerChangedEventManager(admin);
        this.processingUnitStatusChangedEventManager = new DefaultProcessingUnitStatusChangedEventManager(admin);
        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);
        this.spaceCorrelatedEventManager = new DefaultProcessingUnitSpaceCorrelatedEventManager(this);
        this.processingUnitInstanceStatisticsChangedEventManager = new DefaultProcessingUnitInstanceStatisticsChangedEventManager(admin);
    }

    public ProcessingUnits getProcessingUnits() {
        return this.processingUnits;
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public String getName() {
        return this.name;
    }

    public BeanLevelProperties getBeanLevelProperties() {
        return beanLevelProperties;
    }

    public ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged() {
        return this.managingGridServiceManagerChangedEventManager;
    }

    public BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged() {
        return this.backupGridServiceManagerChangedEventManager;
    }

    public Space getSpace() {
        Iterator<Space> it = spaces.values().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public Space[] getSpaces() {
        return this.spaces.values().toArray(new Space[0]);
    }

    public void addEmbeddedSpace(Space space) {
        assertStateChangesPermitted();
        Space existingSpace = spaces.putIfAbsent(space.getName(), space);
        if (existingSpace == null) {
            spaceCorrelatedEventManager.processingUnitSpaceCorrelated(new ProcessingUnitSpaceCorrelatedEvent(space, this));
        }
    }

    public ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged() {
        return this.processingUnitStatusChangedEventManager;
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return this.processingUnitInstanceAddedEventManager;
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return this.processingUnitInstanceRemovedEventManager;
    }

    public ProcessingUnitSpaceCorrelatedEventManager getSpaceCorrelated() {
        return this.spaceCorrelatedEventManager;
    }

    public void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().add(eventListener);
        getProcessingUnitInstanceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().remove(eventListener);
        getProcessingUnitInstanceRemoved().remove(eventListener);
    }

    public int getNumberOfInstances() {
        return this.numberOfInstances;
    }

    public void setNumberOfInstances(int numberOfInstances) {
        assertStateChangesPermitted();
        this.numberOfInstances = numberOfInstances;
    }

    public int getNumberOfBackups() {
        return this.numberOfBackups;
    }

    public void setNumberOfBackups(int numberOfBackups) {
        assertStateChangesPermitted();
        this.numberOfBackups = numberOfBackups;
    }

    public int getTotalNumberOfInstances() {
        return getNumberOfInstances() * (getNumberOfBackups() + 1);
    }

    public int getMaxInstancesPerVM() {
        return sla.getMaxInstancesPerVM();
    }

    public int getMaxInstancesPerMachine() {
        return sla.getMaxInstancesPerMachine();
    }

    public Map<String, Integer> getMaxInstancesPerZone() {
        return Collections.unmodifiableMap(sla.getMaxInstancesPerZone());
    }

    public String[] getRequiredZones() {
        ArrayList<String> zones = new ArrayList<String>();
        for (Requirement req : sla.getRequirements()) {
            if (req instanceof ZoneRequirement) {
                zones.add(((ZoneRequirement) req).getZone());
            }
        }
        return zones.toArray(new String[zones.size()]);
    }

    public DeploymentStatus getStatus() {
        return this.deploymentStatus;
    }

    public boolean waitFor(int numberOfProcessingUnitInstances) {
        return waitFor(numberOfProcessingUnitInstances, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                latch.countDown();
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public Space waitForSpace() {
        return waitForSpace(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public Space waitForSpace(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Space> ref = new AtomicReference<Space>();
        ProcessingUnitSpaceCorrelatedEventListener correlated = new ProcessingUnitSpaceCorrelatedEventListener() {
            public void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event) {
                ref.set(event.getSpace());
                latch.countDown();
            }
        };
        getSpaceCorrelated().add(correlated);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getSpaceCorrelated().remove(correlated);
        }
    }

    public GridServiceManager waitForManaged() {
        return waitForManaged(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public GridServiceManager waitForManaged(long timeout, TimeUnit timeUnit) {
        if (isManaged()) {
            return managingGridServiceManager;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        ManagingGridServiceManagerChangedEventListener listener = new ManagingGridServiceManagerChangedEventListener() {
            public void processingUnitManagingGridServiceManagerChanged(ManagingGridServiceManagerChangedEvent event) {
                ref.set(event.getNewGridServiceManager());
                latch.countDown();
            }
        };
        getManagingGridServiceManagerChanged().add(listener);
        if (isManaged()) {
            getManagingGridServiceManagerChanged().remove(listener);
            return managingGridServiceManager;
        }
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getManagingGridServiceManagerChanged().remove(listener);
        }
    }

    public boolean canIncrementInstance() {
        return getSpaces().length == 0;
    }

    public boolean canDecrementInstance() {
        return getSpaces().length == 0;
    }

    public void incrementInstance() {
        if (!isManaged()) {
            throw new AdminException("No managing grid service manager for processing unit");
        }
        ((InternalGridServiceManager) managingGridServiceManager).incrementInstance(this);
    }

    public void decrementInstance() {
        Iterator<ProcessingUnitInstance> it = iterator();
        if (it.hasNext()) {
            ((InternalGridServiceManager) managingGridServiceManager).decrementInstance(it.next());
        }
    }

    public GridServiceManager getManagingGridServiceManager() {
        return this.managingGridServiceManager;
    }

    public GridServiceManager[] getBackupGridServiceManagers() {
        return this.backupGridServiceManagers.values().toArray(new GridServiceManager[0]);
    }

    public boolean isManaged() {
        return managingGridServiceManager != null;
    }

    public GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID) {
        return backupGridServiceManagers.get(gridServiceManagerUID);
    }

    public void undeploy() {
        if (!isManaged()) {
            throw new AdminException("No managing GSM to undeploy from");
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitRemovedEventListener listener = new ProcessingUnitRemovedEventListener() {
            public void processingUnitRemoved(ProcessingUnit processingUnit) {
                if (getName().equals(processingUnit.getName())) {
                    latch.countDown();
                }
            }
        };
        
        getProcessingUnits().getProcessingUnitRemoved().add(listener);
        try {
            ((InternalGridServiceManager) managingGridServiceManager).undeployProcessingUnit(getName());
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new AdminException("Failed to undeploy", e);
            }
        }finally {
            getProcessingUnits().getProcessingUnitRemoved().remove(listener);
        }
    }

    public void setManagingGridServiceManager(GridServiceManager gridServiceManager) {
        assertStateChangesPermitted();
        final GridServiceManager previousManaging = this.managingGridServiceManager;
        final GridServiceManager newManaging = gridServiceManager;

        this.managingGridServiceManager = gridServiceManager;
        ManagingGridServiceManagerChangedEvent event = new ManagingGridServiceManagerChangedEvent(this, newManaging, previousManaging);
        managingGridServiceManagerChangedEventManager.processingUnitManagingGridServiceManagerChanged(event);
        ((InternalManagingGridServiceManagerChangedEventManager) processingUnits.getManagingGridServiceManagerChanged()).processingUnitManagingGridServiceManagerChanged(event);
    }

    public void addBackupGridServiceManager(final GridServiceManager backupGridServiceManager) {
        assertStateChangesPermitted();
        GridServiceManager gridServiceManager = this.backupGridServiceManagers.put(backupGridServiceManager.getUid(), backupGridServiceManager);
        if (gridServiceManager == null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.ADDED, backupGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    public void removeBackupGridServiceManager(String gsmUID) {
        assertStateChangesPermitted();
        final GridServiceManager existingGridServiceManager = backupGridServiceManagers.remove(gsmUID);
        if (existingGridServiceManager != null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.REMOVED, existingGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    public boolean setStatus(int statusCode) {
        assertStateChangesPermitted();
        DeploymentStatus tempStatus;
        switch (statusCode) {
            case 0:
                tempStatus = DeploymentStatus.UNDEPLOYED;
                break;
            case 1:
                tempStatus = DeploymentStatus.SCHEDULED;
                break;
            case 2:
                tempStatus = DeploymentStatus.DEPLOYED;
                break;
            case 3:
                tempStatus = DeploymentStatus.BROKEN;
                break;
            case 4:
                tempStatus = DeploymentStatus.COMPROMISED;
                break;
            case 5:
                tempStatus = DeploymentStatus.INTACT;
                break;
            default:
                throw new IllegalStateException("No status match");
        }
        if (tempStatus != deploymentStatus) {
            ProcessingUnitStatusChangedEvent event = new ProcessingUnitStatusChangedEvent(this, deploymentStatus, tempStatus);
            processingUnitStatusChangedEventManager.processingUnitStatusChanged(event);
            ((InternalProcessingUnitStatusChangedEventManager) processingUnits.getProcessingUnitStatusChanged()).processingUnitStatusChanged(event);
            deploymentStatus = tempStatus;
            return true;
        }
        deploymentStatus = tempStatus;
        return false;
    }

    public Iterator<ProcessingUnitInstance> iterator() {
        return Collections.unmodifiableCollection(processingUnitInstances.values()).iterator();
    }

    public ProcessingUnitInstance[] getInstances() {
        return processingUnitInstances.values().toArray(new ProcessingUnitInstance[0]);
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return getInstances();
    }

    public ProcessingUnitPartition[] getPartitions() {
        return processingUnitPartitions.values().toArray(new ProcessingUnitPartition[0]);
    }

    public ProcessingUnitPartition getPartition(int partitionId) {
        return processingUnitPartitions.get(partitionId);
    }

    public void addProcessingUnitInstance(final ProcessingUnitInstance processingUnitInstance) {
        assertStateChangesPermitted();
        final ProcessingUnitInstance existingProcessingUnitInstance = processingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
        InternalProcessingUnitPartition partition = getPartition(processingUnitInstance);
        partition.addProcessingUnitInstance(processingUnitInstance);
        ((InternalProcessingUnitInstance) processingUnitInstance).setProcessingUnitPartition(partition);

        // handle events
        if (existingProcessingUnitInstance == null) {
            processingUnitInstance.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
            processingUnitInstance.setStatisticsHistorySize(statisticsHistorySize);
            if (isMonitoring()) {
                processingUnitInstance.startStatisticsMonitor();
            }
            processingUnitInstanceAddedEventManager.processingUnitInstanceAdded(processingUnitInstance);
            ((InternalProcessingUnitInstanceAddedEventManager) processingUnits.getProcessingUnitInstanceAdded()).processingUnitInstanceAdded(processingUnitInstance);
        }
    }

    public void removeProcessingUnitInstance(String uid) {
        final ProcessingUnitInstance processingUnitInstance = processingUnitInstances.remove(uid);
        if (processingUnitInstance != null) {
            processingUnitInstance.stopStatisticsMonitor();
            InternalProcessingUnitPartition partition = getPartition(processingUnitInstance);
            partition.removeProcessingUnitInstance(uid);

            processingUnitInstanceRemovedEventManager.processingUnitInstanceRemoved(processingUnitInstance);
            ((InternalProcessingUnitInstanceRemovedEventManager) processingUnits.getProcessingUnitInstanceRemoved()).processingUnitInstanceRemoved(processingUnitInstance);
        }
    }

    private InternalProcessingUnitPartition getPartition(ProcessingUnitInstance processingUnitInstance) {
        InternalProcessingUnitPartition partition;
        if (numberOfBackups == 0) {
            partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(0));
        } else {
            partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(processingUnitInstance.getInstanceId() - 1));
        }
        return partition;
    }

    public ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged() {
        return this.processingUnitInstanceStatisticsChangedEventManager;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.setStatisticsInterval(interval, timeUnit);
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        this.statisticsHistorySize = historySize;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.setStatisticsHistorySize(historySize);
        }
    }

    public synchronized void startStatisticsMonitor() {
        scheduledStatisticsMonitor = true;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMonitor() {
        scheduledStatisticsMonitor = false;
        for (ProcessingUnitInstance processingUnitInstance : processingUnitInstances.values()) {
            processingUnitInstance.stopStatisticsMonitor();
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultProcessingUnit that = (DefaultProcessingUnit) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

    public void scale(BeanConfig strategyConfig) {

        ProcessingUnitElasticConfig config = getElasticConfig();
        config.setScaleStrategy(strategyConfig);
        setElasticConfig(config);
    }

    public ProcessingUnitElasticConfig getElasticConfig() {
        if (getManagingGridServiceManager() == null) {
            throw new AdminException("Processing Unit " + getName() + " does not have an associated managing GSM");
        }
        return ((InternalGridServiceManager)getManagingGridServiceManager()).getProcessingUnitElasticConfig(this);
    }

    public void setElasticConfig(ProcessingUnitElasticConfig config) {
        if (getManagingGridServiceManager() == null) {
            throw new AdminException("Processing Unit " + getName() + " does not have an associated managing GSM");
        }
        ((InternalGridServiceManager)getManagingGridServiceManager()).setProcessingUnitElasticConfig(this,config);
        
    }
}
