package org.openspaces.admin.pu;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.space.Space;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface ProcessingUnit extends Iterable<ProcessingUnitInstance>, AdminAware {

    String getName();

    int getNumberOfInstances();

    int getNumberOfBackups();

    DeploymentStatus getStatus();

    /**
     * Waits till at least the provided number of Processing Unit Instances are up.
     */
    boolean waitFor(int numberOfProcessingUnitInstances);

    /**
     * Waits till at least the provided number of Processing Unit Instances are up for the specified timeout.
     */
    boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    /**
     * Waits till an embedded Space is correlated with the processing unit.
     */
    Space waitForSpace();

    /**
     * Waits till an embedded Space is correlated with the processing unit for the specified timeout.
     */
    Space waitForSpace(long timeout, TimeUnit timeUnit);

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
     * Returns all the embedded spaces within a processing unit. Returns an empty array if there
     * are no embedded spaces defined within the processing unit, or none has been associated with
     * the processing unit yet.
     */
    Space[] getSpaces();

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

    ProcessingUnitSpaceCorrelatedEventManager getSpaceCorrelated();
}
