package org.openspaces.admin.application.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.application.Application;

/**
 * An event listener allowing to listen for {@link org.openspaces.admin.pu.Application} removal (undeployment).
 *
 * @author itaif
 * @see org.openspaces.admin.application.Applications#getApplicationRemoved()
 */
public interface ApplicationRemovedEventListener extends AdminEventListener {

    /**
     * A callback indicating that an Application was removed (undeployed).
     */
    void applicationRemoved(Application application);
}