package org.openspaces.admin.internal.application.events;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.events.ApplicationRemovedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author itaif
 */
public class ClosureApplicationRemovedEventListener extends AbstractClosureEventListener implements ApplicationRemovedEventListener {

    public ClosureApplicationRemovedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void applicationRemoved(Application application) {
        getClosure().call(application);
    }

}
