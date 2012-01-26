package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove
 * {@link ProcessingUnitInstanceProvisionStatusChangedEventListener}s, in order to listen to
 * {@link ProcessingUnitInstanceProvisionStatusChangedEvent}s.
 * 
 * @since 8.0.6
 * @author moran
 */
public interface ProcessingUnitInstanceProvisionStatusChangedEventManager {
    
    /**
     * Adds an event listener. Note, the add callback will be called for each currently held
     * processing unit instance provision status.
     */
    public void add(ProcessingUnitInstanceProvisionStatusChangedEventListener listener);
    
    /**
     * Adds an event listener. Allows to control if the event will be called with the current status
     * held for each processing unit instance.
     */
    public void add(ProcessingUnitInstanceProvisionStatusChangedEventListener listener, boolean includeCurrentStatus);
    
    /**
     * Removes an event listener.
     */
    public void remove(ProcessingUnitInstanceProvisionStatusChangedEventListener listener);
}
