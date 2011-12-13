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
    private final ProcessingUnitInstanceProvisionFailureEvent failureEvent;

    public ProcessingUnitInstanceProvisionPendingEvent(ProcessingUnit processingUnit, String processingUnitInstanceName, ProcessingUnitInstanceProvisionFailureEvent failureEvent) {
        this.processingUnit = processingUnit;
        this.processingUnitInstanceName = processingUnitInstanceName;
        this.failureEvent = failureEvent;
    }
 
    /**
     * @return The processing unit this instance belongs to.
     */
    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
    
    /**
     * @return The processing unit instance name, equivalent to {@link ProcessingUnitInstance#getProcessingUnitInstanceName()}.
     */
    public String getProcessingUnitInstanceName() {
        return processingUnitInstanceName;
    }
    
    /**
     * When an attempt to provision a processing unit instance results in a failure, the
     * <tt>ProcessingUnitInstanceProvisionPendingEvent</tt> will hold a reference to the
     * <tt>ProcessingUnitInstanceProvisionFailureEvent</tt>. <br>
     * When there are no available GSCs, a <tt>ProcessingUnitInstanceProvisionPendingEvent</tt> will
     * not provide any failure reason, thus returning <code>null</code>.
     * 
     * @return the previous failure event or <code>null</code>.
     */
    public ProcessingUnitInstanceProvisionFailureEvent getProcessingUnitInstanceProvisionFailureEvent() {
        return failureEvent;
    }
}
