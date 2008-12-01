package org.openspaces.admin.internal.lus.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.lus.InternalLookupServices;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultLookupServiceRemovedEventManager implements InternalLookupServiceRemovedEventManager {

    private final InternalLookupServices lookupServices;

    private final InternalAdmin admin;

    private final List<LookupServiceRemovedEventListener> listeners = new CopyOnWriteArrayList<LookupServiceRemovedEventListener>();

    public DefaultLookupServiceRemovedEventManager(InternalLookupServices lookupServices) {
        this.lookupServices = lookupServices;
        this.admin = (InternalAdmin) lookupServices.getAdmin();
    }

    public void lookupServiceRemoved(final LookupService lookupService) {
        for (final LookupServiceRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.lookupServiceRemoved(lookupService);
                }
            });
        }
    }

    public void add(final LookupServiceRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(LookupServiceRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureLookupServiceRemovedEventListener(eventListener));
        } else {
            add((LookupServiceRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureLookupServiceRemovedEventListener(eventListener));
        } else {
            remove((LookupServiceRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}