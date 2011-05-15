package org.openspaces.admin.application.events;


/**
 * An event manager allowing to add and remove {@link ApplicationAddedEventListener}s.
 * 
 * @author itaif
 * @see org.openspaces.admin.pu.Applications#getApplicationsAdded()
 */
public interface ApplicationAddedEventManager {

    /**
     * Adds an event listener.
     */
    void add(ApplicationAddedEventListener eventListener);

    /**
     * Removes an event listener.
     */
    void remove(ApplicationAddedEventListener eventListener);
}