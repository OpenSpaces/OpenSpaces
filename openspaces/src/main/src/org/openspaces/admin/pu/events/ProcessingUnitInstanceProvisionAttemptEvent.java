package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event indicating that an attempt to provision a processing unit instance occurred. An instance
 * is provisioned on a {@link GridServiceContainer} resulting in either success or failure. On
 * success, a {@link ProcessingUnitInstance} is created and available (see
 * {@link ProcessingUnitInstanceProvisionSuccessEventListener}). On failure, the failed instance is
 * scheduled to re-provision (see {@link ProcessingUnitInstanceProvisionFailureEventListener} and
 * {@link ProcessingUnitInstanceProvisionPendingEventListener}).
 * 
 * @author moran
 * @since 8.0.6
 */
public class ProcessingUnitInstanceProvisionAttemptEvent {

    private final GridServiceContainer gridServiceContainer;
    private final ProcessingUnit processingUnit;
    private final String processingUnitInstanceName;

    public ProcessingUnitInstanceProvisionAttemptEvent(GridServiceContainer gridServiceContainer, ProcessingUnit processingUnit, String processingUnitInstanceName) {
        this.gridServiceContainer = gridServiceContainer;
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
    }
    
    /**
     * @return The Grid Service Container on which the instance is being provisioned upon.
     */
    public GridServiceContainer getGridServiceContainer() {
        return gridServiceContainer;
    }
    
    /**
     * @return The processing unit this instance belongs to.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
    
    /**
     * @return The processing unit instance name equivalent to {@link ProcessingUnitInstance#getProcessingUnitInstanceName()})
     */
    public String getProcessingUnitInstanceName() {
        return processingUnitInstanceName;
    }
}
