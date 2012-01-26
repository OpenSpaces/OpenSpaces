package org.openspaces.admin.internal.pu.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;

public class DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager implements
        InternalProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager {

    private final InternalAdmin admin;

    private final List<ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener> eventListeners = new CopyOnWriteArrayList<ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener>();

    private final InternalProcessingUnit processingUnit;

    public DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(InternalAdmin admin) {
        this(admin, null);
    }
    
    public DefaultProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager(InternalAdmin admin, InternalProcessingUnit processingUnit) {
        this.admin = admin;
        this.processingUnit = processingUnit;
    }

    
    @Override
    public void add(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener) {
        add(listener, true);
    }
    
    @Override
    public void add(final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener, boolean includeCurrentStatus) {
        if (includeCurrentStatus) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    if (processingUnit != null) {
                        notifyListener(listener, processingUnit);
                    } else {
                        for (ProcessingUnit pu : admin.getProcessingUnits()) {
                            notifyListener(listener, pu);
                        }
                    }
                }

                private void notifyListener(
                        final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener,
                        ProcessingUnit pu) {
                    for (ProcessingUnitInstance instance : pu) {
                        listener.processingUnitInstanceMemberAliveIndicatorStatusChanged(new ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent(
                                instance, null, instance.getMemberAliveIndicatorStatus()));
                    }
                }
            });
        }      
        eventListeners.add(listener);
    }

    @Override
    public void remove(ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void processingUnitInstanceMemberAliveIndicatorStatusChanged(final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEvent event) {
        for (final ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener listener : eventListeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitInstanceMemberAliveIndicatorStatusChanged(event);
                }
            });
        }
    }
}
