package org.openspaces.admin.internal.application.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.events.ApplicationRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.application.InternalApplications;
import org.openspaces.admin.internal.support.GroovyHelper;

public class DefaultApplicationRemovedEventManager implements InternalApplicationRemovedEventManager {

    private final InternalApplications applications;

    private final InternalAdmin admin;

    private final List<ApplicationRemovedEventListener> applicationRemovedEventListeners = new CopyOnWriteArrayList<ApplicationRemovedEventListener>();

    public DefaultApplicationRemovedEventManager(InternalApplications applications) {
        this.applications = applications;
        this.admin = (InternalAdmin) applications.getAdmin();
    }

    @Override
    public void applicationRemoved(final Application application) {
        for (final ApplicationRemovedEventListener listener : applicationRemovedEventListeners) {
            admin.pushEvent(listener, new Runnable() {
                @Override
                public void run() {
                    listener.applicationRemoved(application);
                }
            });
        }
    }

    @Override
    public void add(ApplicationRemovedEventListener eventListener) {
        applicationRemovedEventListeners.add(eventListener);
    }

    @Override
    public void remove(ApplicationRemovedEventListener eventListener) {
        applicationRemovedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureApplicationRemovedEventListener(eventListener));
        } else {
            add((ApplicationRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureApplicationRemovedEventListener(eventListener));
        } else {
            remove((ApplicationRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }

}
