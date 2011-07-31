package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultProcessingUnitStatusChangedEventManager implements InternalProcessingUnitStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitStatusChangedEventListener> listeners = new CopyOnWriteArrayList<ProcessingUnitStatusChangedEventListener>();

    private final ProcessingUnit processingUnit;

    public DefaultProcessingUnitStatusChangedEventManager(InternalAdmin admin) {
        this(admin, null);
    }
    
    public DefaultProcessingUnitStatusChangedEventManager(InternalAdmin admin, ProcessingUnit processingUnit) {
        this.admin = admin;
        this.processingUnit = processingUnit;
    }

    public void processingUnitStatusChanged(final ProcessingUnitStatusChangedEvent event) {
        for (final ProcessingUnitStatusChangedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitStatusChanged(event);
                }
            });
        }
    }

    public void add(ProcessingUnitStatusChangedEventListener eventListener) {
        add(eventListener, true);
    }
    
    public void add(final ProcessingUnitStatusChangedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    if (processingUnit == null) {
                        for (ProcessingUnit pu : admin.getProcessingUnits()) {
                            eventListener.processingUnitStatusChanged(new ProcessingUnitStatusChangedEvent(pu, null, pu.getStatus()));
                        }
                    } else {
                        eventListener.processingUnitStatusChanged(new ProcessingUnitStatusChangedEvent(processingUnit, null, processingUnit.getStatus()));
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void remove(ProcessingUnitStatusChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureProcessingUnitStatusChangedEventListener(eventListener));
        } else {
            add((ProcessingUnitStatusChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureProcessingUnitStatusChangedEventListener(eventListener));
        } else {
            remove((ProcessingUnitStatusChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}