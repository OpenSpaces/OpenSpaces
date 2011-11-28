package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link ProcessingUnitInstanceProvisionPendingEventListener}s.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionPending()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionPending()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionPendingEventManager {

    /**
     * Adds an event listener, events will be raised if an instance is pending to be provisioned.
     */
    public void add(ProcessingUnitInstanceProvisionPendingEventListener listener);
    
    /**
     * Removes an event listener.
     */
    public void remove(ProcessingUnitInstanceProvisionPendingEventListener listener);
    
}
