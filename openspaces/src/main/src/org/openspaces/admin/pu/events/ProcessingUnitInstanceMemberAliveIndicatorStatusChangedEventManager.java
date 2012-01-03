package org.openspaces.admin.pu.events;

/**
 * An event manager allowing to add and remove
 * {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener}s, in order to listen to
 * {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent}s.
 * 
 * @since 8.0.6
 * @author moran
 */
public interface ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager {

    /**
     * Adds an event listener. Note, the add callback will be called for currently discovered
     * processing unit instances as well with the current member alive indicator status.
     */
    void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener);
    
    /**
     * Adds an event listener. Allows to control if the event will be called with the current status for existing processing
     * unit instances as well.
     */
    void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener, boolean includeCurrentStatus);
    
    /**
     * Removes an event listener.
     */
    void remove(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener);
}
