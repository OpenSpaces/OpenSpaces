package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstanceRemovedEventManager implements InternalProcessingUnitInstanceRemovedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceRemovedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitInstanceRemovedEventListener>();

    public DefaultProcessingUnitInstanceRemovedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void processingUnitInstanceRemoved(final ProcessingUnitInstance processingUnitInstance) {
        for (final ProcessingUnitInstanceRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceRemoved(processingUnitInstance);
                }
            });
        }
    }

    public void add(ProcessingUnitInstanceRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(ProcessingUnitInstanceRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitInstanceRemovedEventListener(eventListener));
        } else {
            add((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitInstanceRemovedEventListener(eventListener));
        } else {
            remove((ProcessingUnitInstanceRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}