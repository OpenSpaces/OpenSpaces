package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener allowing to listen for {@link ProcessingUnitInstanceProvisionAttemptEvent}s.
 * An attempt to provision is either followed by a successful event, a failure event or a pending event.
 *
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionAttempt()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionAttempt()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionAttemptEventListener extends AdminEventListener {

    /**
     * A callback indicating that an attempt to provision an instance is in progress.
     */
    void attempt(ProcessingUnitInstanceProvisionAttemptEvent processingUnitInstanceProvisionAttemptEvent);
}
