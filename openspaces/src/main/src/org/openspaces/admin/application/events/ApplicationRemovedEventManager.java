package org.openspaces.admin.application.events;


/**
 * An event manager allowing to add and remove {@link ApplicationRemovedEventListener}s.
 * 
 * @author itaif
 * @see org.openspaces.admin.pu.Applications#getApplicationsRemoved()
 */
public interface ApplicationRemovedEventManager {

    /**
     * Adds an event listener.
     */
    void add(ApplicationRemovedEventListener eventListener);

    /**
     * Removes an event listener.
     */
    void remove(ApplicationRemovedEventListener eventListener);

}
