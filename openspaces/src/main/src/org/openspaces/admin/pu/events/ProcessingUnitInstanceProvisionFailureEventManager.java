package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link ProcessingUnitInstanceProvisionFailureEventListener}s.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionFailure()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionFailure()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionFailureEventManager {

    /**
     * Adds an event listener, events will be raised if an instance has failed to be provisioned.
     */
    public void add(ProcessingUnitInstanceProvisionFailureEventListener listener);
    
    /**
     * Removes an event listener.
     */
    public void remove(ProcessingUnitInstanceProvisionFailureEventListener listener);
    
}
