package org.openspaces.admin.pu.events;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event indicating that a failure to provision a processing unit instance. An attempt to
 * provision an instance on a {@link GridServiceContainer} resulted in a failure. The failed
 * instance is scheduled to re-provision (see
 * {@link ProcessingUnitInstanceProvisionPendingEventListener}).
 * 
 * @author moran
 * @since 8.0.6
 */
public class ProcessingUnitInstanceProvisionFailureEvent {
    private final GridServiceContainer gridServiceContainer;
    private final ProcessingUnit processingUnit;
    private final String processingUnitInstanceName;
    private final ProcessingUnitInstanceProvisionFailureException exception;

    public ProcessingUnitInstanceProvisionFailureEvent(GridServiceContainer gridServiceContainer,
            ProcessingUnit processingUnit, String processingUnitInstanceName, ProcessingUnitInstanceProvisionFailureException exception) {
        this.gridServiceContainer = gridServiceContainer;
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
        this.exception = exception;
    }
 
    /**
     * @return The Grid Service Container this instance was attempted to be provisioned upon.
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
     * The processing unit instance name, equivalent to {@link ProcessingUnitInstance#getProcessingUnitInstanceName()}.
     * @return
     */
    public String getProcessingUnitInstanceName() {
        return processingUnitInstanceName;
    }
    
    /**
     * @return An exception that occurred while trying to instantiate the instance. May be <code>null</code>.
     */
    public ProcessingUnitInstanceProvisionFailureException getException() {
        return exception;
    }
}
