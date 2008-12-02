package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public class ProcessingUnitStatusChangedEvent {

    private final ProcessingUnit processingUnit;

    private final DeploymentStatus previousStatus;

    private final DeploymentStatus newStatus;

    public ProcessingUnitStatusChangedEvent(ProcessingUnit processingUnit, DeploymentStatus previousStatus, DeploymentStatus newStatus) {
        this.processingUnit = processingUnit;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }

    public DeploymentStatus getPreviousStatus() {
        return previousStatus;
    }

    public DeploymentStatus getNewStatus() {
        return newStatus;
    }
}
