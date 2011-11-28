package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event listener allowing to listen for pending events of processing unit instances matching
 * {@link ProcessingUnitInstance#getProcessingUnitInstanceName()}.
 * A pending event is usually followed by an attempt to re-provision event.
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
    void pending(String processingUnitInstanceName);
    
}
