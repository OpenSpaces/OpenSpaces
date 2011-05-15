package org.openspaces.admin.application.events;


/**
 * A simple life-cycle event listener that implements both the application added and zone removed event listeners.
 *
 * @author itaif
 */
public interface ApplicationLifecycleEventListener extends ApplicationAddedEventListener, ApplicationRemovedEventListener {
    
}