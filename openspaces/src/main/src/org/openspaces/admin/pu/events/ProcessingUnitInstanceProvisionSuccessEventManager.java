package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link ProcessingUnitInstanceProvisionSuccessEventListener}s.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionSuccess()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionSuccess()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionSuccessEventManager {

    /**
     * Adds an event listener, events will be raised if an instance is successfully provisioned.
     */
    public void add(ProcessingUnitInstanceProvisionSuccessEventListener listener);
    
    /**
     * Removes an event listener.
     */
    public void remove(ProcessingUnitInstanceProvisionSuccessEventListener listener);
    
}
