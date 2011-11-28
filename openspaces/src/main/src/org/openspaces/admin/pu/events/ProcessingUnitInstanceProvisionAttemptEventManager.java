package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove {@link ProcessingUnitInstanceProvisionAttemptEventListener}s.
 * 
 * @see org.openspaces.admin.pu.ProcessingUnit#getProcessingUnitInstanceProvisionAttempt()
 * @see org.openspaces.admin.pu.ProcessingUnits#getProcessingUnitInstanceProvisionAttempt()
 * 
 * @author moran
 * @since 8.0.6
 */
public interface ProcessingUnitInstanceProvisionAttemptEventManager {

    /**
     * Adds an event listener, events will be raised if an attempt to provision an instance is in progress.
     */
    public void add(ProcessingUnitInstanceProvisionAttemptEventListener listener);
    
    /**
     * Removes an event listener.
     */
    public void remove(ProcessingUnitInstanceProvisionAttemptEventListener listener);
    
}
