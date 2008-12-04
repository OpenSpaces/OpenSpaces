package org.openspaces.admin.pu;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.Spaces;

/**
 * @author kimchy
 */
public interface ProcessingUnit extends Iterable<ProcessingUnitInstance> {

    String getName();

    int getNumberOfInstances();

    int getNumberOfBackups();

    DeploymentStatus getStatus();

    /**
     * Returns <code>true</code> if there is a managing GSM for it.
     */
    boolean isManaged();

    GridServiceManager getManagingGridServiceManager();

    GridServiceManager[] getBackupGridServiceManagers();

    GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID);

    /**
     * Returns the (first) embedded space within a processing unit. Returns <code>null</code> if
     * no embedded space is defined within the processing unit or if no processing unit instance
     * has been added to the processing unit.
     */
    Space getSpace();

    /**
     * Returns the embedded spaces within this processing unit.
     */
    Spaces getSpaces();

    ProcessingUnitInstance[] getInstances();

    ProcessingUnitPartition[] getPartitions();

    ProcessingUnitPartition getPartition(int partitionId);

    void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged();

    BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged();

    ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();
}
