package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event indicating that a processing unit instance is pending to be re-provisioned.
 * 
 * @author moran
 * @since 8.0.6
 */
public class ProcessingUnitInstanceProvisionPendingEvent {
    private final ProcessingUnit processingUnit;
    private final String processingUnitInstanceName;

    public ProcessingUnitInstanceProvisionPendingEvent(ProcessingUnit processingUnit, String processingUnitInstanceName) {
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
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
}
