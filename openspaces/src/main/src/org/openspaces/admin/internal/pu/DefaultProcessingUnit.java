package org.openspaces.admin.internal.pu;

import com.gigaspaces.grid.gsm.PUDetails;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;

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

    private volatile DeploymentStatus deploymentStatus = null;

    private volatile GridServiceManager managingGridServiceManager;

    private final Map<String, GridServiceManager> backupGridServiceManagers = new ConcurrentHashMap<String, GridServiceManager>();

    private final Map<String, ProcessingUnitInstance> processingUnitInstances = new ConcurrentHashMap<String, ProcessingUnitInstance>();

    private final Map<Integer, ProcessingUnitPartition> processingUnitPartitions = new ConcurrentHashMap<Integer, ProcessingUnitPartition>();

    public DefaultProcessingUnit(InternalAdmin admin, InternalProcessingUnits processingUnits, PUDetails details) {
        this.admin = admin;
        this.processingUnits = processingUnits;
        this.name = details.getName();
        this.numberOfInstances = details.getNumberOfInstances();
        this.numberOfBackups = details.getNumberOfBackups();
        setStatus(details.getStatus());

        for (int i = 0; i < numberOfInstances; i++) {
            processingUnitPartitions.put(i, new DefaultProcessingUnitPartition(this, i));
        }
    }

    public String getName() {
        return this.name;
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
        final GridServiceManager oldManaging = this.managingGridServiceManager;
        final GridServiceManager newManaging = gridServiceManager;
        this.managingGridServiceManager = gridServiceManager;
        for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    if (newManaging == null) {
                        listener.processingUnitManagingGridServiceManagerUnknown(DefaultProcessingUnit.this);
                    } else {
                        listener.processingUnitManagingGridServiceManagerSet(DefaultProcessingUnit.this, oldManaging, newManaging);
                    }
                }
            });
        }
    }

    public void addBackupGridServiceManager(final GridServiceManager backupGridServiceManager) {
        GridServiceManager gridServiceManager = this.backupGridServiceManagers.put(backupGridServiceManager.getUid(), backupGridServiceManager);
        if (gridServiceManager == null) {
            for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitBackupGridServiceManagerAdded(DefaultProcessingUnit.this, backupGridServiceManager);
                    }
                });
            }
        }
    }

    public void removeBackupGridServiceManager(String gsmUID) {
        final GridServiceManager existingGridServiceManager = backupGridServiceManagers.remove(gsmUID);
        if (existingGridServiceManager != null) {
            for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitBackupGridServiceManagerRemoved(DefaultProcessingUnit.this, existingGridServiceManager);
                    }
                });
            }
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
        if (deploymentStatus != null && tempStatus != deploymentStatus) {
            final ProcessingUnit thisPU = this;
            final DeploymentStatus oldDeploymentStatus = deploymentStatus;
            final DeploymentStatus newDeploymentStatus = tempStatus;
            for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
                admin.raiseEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitStatusChanged(thisPU, oldDeploymentStatus, newDeploymentStatus);
                    }
                });
            }
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
            for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitInstanceAdded(processingUnitInstance);
                    }
                });
            }
        }
    }

    public void removeProcessingUnitInstance(String uid) {
        final ProcessingUnitInstance processingUnitInstance = processingUnitInstances.remove(uid);
        if (processingUnitInstance != null) {
            InternalProcessingUnitPartition partition = ((InternalProcessingUnitPartition) processingUnitPartitions.get(processingUnitInstance.getInstanceId() - 1));
            partition.removeProcessingUnitInstance(uid);

            for (final ProcessingUnitEventListener listener : processingUnits.getEventListeners()) {
                admin.pushEvent(listener, new Runnable() {
                    public void run() {
                        listener.processingUnitInstanceRemoved(processingUnitInstance);
                    }
                });
            }
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
