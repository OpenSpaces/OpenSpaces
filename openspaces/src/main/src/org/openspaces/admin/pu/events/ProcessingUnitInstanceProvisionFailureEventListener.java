package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener allowing to listen for {@link ProcessingUnitInstanceProvisionFailureEvent}s.
 * A failure to provision event is followed by a pending event.
 *
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionFailure()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionFailure()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionFailureEventListener extends AdminEventListener {

    /**
     * A callback indicating that an instance has failed to be provisioned.
     */
    void failure(ProcessingUnitInstanceProvisionFailureEvent processingUnitInstanceProvisionFailureEvent);
}
