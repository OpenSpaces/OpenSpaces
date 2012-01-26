package org.openspaces.admin.pu.events;

import org.openspaces.admin.AdminEventListener;

/**
 * An event listener allowing to listen for {@link ProcessingUnitInstanceProvisionStatusChangedEvent}s.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionStatusChanged()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionStatusChanged()
 * 
 * @since 8.0.6
 * @author moran
 */
public interface ProcessingUnitInstanceProvisionStatusChangedEventListener extends AdminEventListener {

    /**
     * A callback indicating the provision status has changed.
     */
    public void processingUnitInstanceProvisionStatusChanged(ProcessingUnitInstanceProvisionStatusChangedEvent event);
}
