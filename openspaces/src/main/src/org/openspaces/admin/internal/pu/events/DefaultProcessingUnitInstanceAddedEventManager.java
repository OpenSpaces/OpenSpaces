package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstancesAware;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitInstanceAddedEventManager implements InternalProcessingUnitInstanceAddedEventManager {

    private final InternalProcessingUnitInstancesAware processingUnits;

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceAddedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitInstanceAddedEventListener>();

    public DefaultProcessingUnitInstanceAddedEventManager(InternalProcessingUnitInstancesAware processingUnits, InternalAdmin admin) {
        this.processingUnits = processingUnits;
        this.admin = admin;
    }

    public void processingUnitInstanceAdded(final ProcessingUnitInstance processingUnitInstance) {
        for (final ProcessingUnitInstanceAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceAdded(processingUnitInstance);
                }
            });
        }
    }

    public void add(final ProcessingUnitInstanceAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (ProcessingUnitInstance processingUnitInstance : processingUnits.getProcessingUnitInstances()) {
                        eventListener.processingUnitInstanceAdded(processingUnitInstance);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void add(final ProcessingUnitInstanceAddedEventListener eventListener) {
        add(eventListener, true);
    }

    public void remove(ProcessingUnitInstanceAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitInstanceAddedEventListener(eventListener));
        } else {
            add((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitInstanceAddedEventListener(eventListener));
        } else {
            remove((ProcessingUnitInstanceAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}