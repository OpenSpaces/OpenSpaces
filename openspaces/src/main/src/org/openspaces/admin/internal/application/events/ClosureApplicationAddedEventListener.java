package org.openspaces.admin.internal.application.events;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.events.ApplicationAddedEventListener;
import org.openspaces.admin.internal.support.AbstractClosureEventListener;

/**
 * @author itaif
 */
public class ClosureApplicationAddedEventListener extends AbstractClosureEventListener implements ApplicationAddedEventListener {

    public ClosureApplicationAddedEventListener(Object closure) {
        super(closure);
    }

    @Override
    public void applicationAdded(Application application) {
        getClosure().call(application);
    }

}
