package org.openspaces.admin.application.events;

import org.openspaces.admin.application.Application;

/**
 * An event listener allowing to listen for {@link org.openspaces.admin.application.Application} additions (deployment).
 *
 * @author itaif
 * @see org.openspaces.admin.application.Applications#getApplicationAdded() 
 */
public interface ApplicationAddedEventListener {

    /**
     * A callback indicating that an Application was added (deployed/discovered).
     */
    void applicationAdded(Application application);
}