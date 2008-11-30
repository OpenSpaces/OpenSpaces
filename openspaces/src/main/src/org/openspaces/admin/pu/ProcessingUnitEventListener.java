package org.openspaces.admin.pu;

import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface ProcessingUnitEventListener {

    void processingUnitAdded(ProcessingUnit processingUnit);

    void processingUnitRemoved(ProcessingUnit processingUnit);

    void processingUnitManagingGridServiceManagerSet(ProcessingUnit processingUnit, GridServiceManager oldManaingGridServiceManager, GridServiceManager newManaingGridServiceManager);

    void processingUnitManagingGridServiceManagerUnknown(ProcessingUnit processingUnit);

    void processingUnitBackupGridServiceManagerAdded(ProcessingUnit processingUnit, GridServiceManager gridServiceManager);

    void processingUnitBackupGridServiceManagerRemoved(ProcessingUnit processingUnit, GridServiceManager gridServiceManager);

    void processingUnitStatusChanged(ProcessingUnit processingUnit, DeploymentStatus oldStatus, DeploymentStatus newStatus);

    void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance);

    void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance);
}
