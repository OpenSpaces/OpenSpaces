package org.openspaces.admin.internal.pu;

import com.gigaspaces.grid.gsm.PUDetails;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.events.*;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultProcessingUnit implements InternalProcessingUnit {

    private final InternalProcessingUnits processingUnits;

    private final InternalAdmin admin;

    private final String name;

    private final int numberOfInstances;

    private final int numberOfBackups;

    private volatile DeploymentStatus deploymentStatus = DeploymentStatus.NA;

    private volatile GridServiceManager managingGridServiceManager;

    private final Map<String, GridServiceManager> backupGridServiceManagers = new ConcurrentHashMap<String, GridServiceManager>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<Integer, ProcessingUnitPartition> processingUnitPartitions = new ConcurrentHashMap<Integer, ProcessingUnitPartition>();

    private final InternalManagingGridServiceManagerChangedEventManager managingGridServiceManagerChangedEventManager;

    private final InternalBackupGridServiceManagerChangedEventManager backupGridServiceManagerChangedEventManager;

    private final InternalProcessingUnitStatusChangedEventManager processingUnitStatusChangedEventManager;

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    public DefaultProcessingUnit(InternalAdmin admin, InternalProcessingUnits processingUnits, PUDetails details) {
        this.admin = admin;
        this.processingUnits = processingUnits;
        this.name = details.getName();
        this.numberOfInstances = details.getNumberOfInstances();
        this.numberOfBackups = details.getNumberOfBackups();

        for (int i = 0; i < numberOfInstances; i++) {
            processingUnitPartitions.put(i, new DefaultProcessingUnitPartition(this, i));
        }

        this.managingGridServiceManagerChangedEventManager = new DefaultManagingGridServiceManagerChangedEventManager(admin);
        this.backupGridServiceManagerChangedEventManager = new DefaultBackupGridServiceManagerChangedEventManager(admin);
        this.processingUnitStatusChangedEventManager = new DefaultProcessingUnitStatusChangedEventManager(admin);
        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);
    }

    public String getName() {
        return this.name;
    }

    public ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged() {
        return this.managingGridServiceManagerChangedEventManager;
    }

    public BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged() {
        return this.backupGridServiceManagerChangedEventManager;
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

    public int getNumberOfBackups() {
        return this.numberOfBackups;
    }

    public DeploymentStatus getStatus() {
        return this.deploymentStatus;
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

    public void setManagingGridServiceManager(GridServiceManager gridServiceManager) {
        final GridServiceManager previousManaging = this.managingGridServiceManager;
        final GridServiceManager newManaging = gridServiceManager;

        this.managingGridServiceManager = gridServiceManager;
        ManagingGridServiceManagerChangedEvent event = new ManagingGridServiceManagerChangedEvent(this, newManaging, previousManaging);
        managingGridServiceManagerChangedEventManager.processingUnitManagingGridServiceManagerChanged(event);
        ((InternalManagingGridServiceManagerChangedEventManager) processingUnits.getManagingGridServiceManagerChanged()).processingUnitManagingGridServiceManagerChanged(event);
    }

    public void addBackupGridServiceManager(final GridServiceManager backupGridServiceManager) {
        GridServiceManager gridServiceManager = this.backupGridServiceManagers.put(backupGridServiceManager.getUid(), backupGridServiceManager);
        if (gridServiceManager == null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.ADDED, backupGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    public void removeBackupGridServiceManager(String gsmUID) {
        final GridServiceManager existingGridServiceManager = backupGridServiceManagers.remove(gsmUID);
        if (existingGridServiceManager != null) {
            BackupGridServiceManagerChangedEvent event = new BackupGridServiceManagerChangedEvent(this, BackupGridServiceManagerChangedEvent.Type.REMOVED, existingGridServiceManager);
            backupGridServiceManagerChangedEventManager.processingUnitBackupGridServiceManagerChanged(event);
            ((InternalBackupGridServiceManagerChangedEventManager) processingUnits.getBackupGridServiceManagerChanged()).processingUnitBackupGridServiceManagerChanged(event);
        }
    }

    public boolean setStatus(int statusCode) {
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
        return processingUnitInstances.values().iterator();
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
        final ProcessingUnitInstance existingProcessingUnitInstance = processingUnitInstances.put(processingUnitInstance.getUid(), processingUnitInstance);
        InternalProcessingUnitPartition partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(processingUnitInstance.getInstanceId() - 1));
        partition.addProcessingUnitInstance(processingUnitInstance);
        ((InternalProcessingUnitInstance) processingUnitInstance).setProcessingUnitPartition(partition);

        // handle events
        if (existingProcessingUnitInstance == null) {
            processingUnitInstanceAddedEventManager.processingUnitInstanceAdded(processingUnitInstance);
            ((InternalProcessingUnitInstanceAddedEventManager) processingUnits.getProcessingUnitInstanceAdded()).processingUnitInstanceAdded(processingUnitInstance);
        }
    }

    public void removeProcessingUnitInstance(String uid) {
        final ProcessingUnitInstance processingUnitInstance = processingUnitInstances.remove(uid);
        if (processingUnitInstance != null) {
            InternalProcessingUnitPartition partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(processingUnitInstance.getInstanceId() - 1));
            partition.removeProcessingUnitInstance(uid);

            processingUnitInstanceRemovedEventManager.processingUnitInstanceRemoved(processingUnitInstance);
            ((InternalProcessingUnitInstanceRemovedEventManager) processingUnits.getProcessingUnitInstanceRemoved()).processingUnitInstanceRemoved(processingUnitInstance);
        }
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
}
