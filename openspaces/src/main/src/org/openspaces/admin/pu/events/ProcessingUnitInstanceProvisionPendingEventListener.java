package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener allowing to listen for {@link ProcessingUnitInstanceProvisionPendingEvent}s. A
 * pending event is fired after an unsatisfied attempt to provision an instance or
 * re-provision it after a previously failed attempt.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionPending()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionPending()
 * @see ProcessingUnitInstanceProvisionAttemptEvent
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionPendingEventListener extends AdminEventListener {

    /**
     * A callback indicating that an instance is pending to be provisioned.
     */
    void pending(ProcessingUnitInstanceProvisionPendingEvent processingUnitInstanceProvisionPendingEvent);
    
}
