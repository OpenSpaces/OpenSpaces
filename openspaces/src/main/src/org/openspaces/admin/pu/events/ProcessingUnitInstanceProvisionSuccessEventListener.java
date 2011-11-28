package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event listener allowing to listen for successful provisioned {@link ProcessingUnitInstance}s.
 *
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionSuccess()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionSuccess()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionSuccessEventListener extends AdminEventListener {

    /**
     * A callback indicating that an instance is successfully provisioned.
     */
    void success(ProcessingUnitInstance instance);
}
