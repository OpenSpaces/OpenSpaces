package org.openspaces.admin.internal.pu.events;

import java.util.List;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionPendingEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionPendingEventListener;

public class DefaultProcessingUnitInstanceProvisionPendingEventManager extends
        AbstractProcessingUnitInstanceProvisionEventManager<ProcessingUnitInstanceProvisionPendingEventListener>
        implements InternalProcessingUnitInstanceProvisionPendingEventManager {

    public DefaultProcessingUnitInstanceProvisionPendingEventManager(InternalAdmin admin) {
        super(admin);
    }
    
    @Override
    public void add(ProcessingUnitInstanceProvisionPendingEventListener listener) {
        super.add(listener);
    }
    
    @Override
    public void remove(ProcessingUnitInstanceProvisionPendingEventListener listener) {
        super.remove(listener);
    }
    

    @Override
    public void raisePendingEvent(ProvisionLifeCycleEvent provisionEvent,
            final ProcessingUnitInstanceProvisionPendingEvent processingUnitInstanceProvisionPendingEvent,
            DefaultProcessingUnitInstanceProvisionEventsManager processingUnitInstanceProvisionEventsManager) {
        
        List<ProcessingUnitInstanceProvisionPendingEventListener> eventListeners = filterListenersBySequenceId(provisionEvent, processingUnitInstanceProvisionEventsManager);
        
        for (final ProcessingUnitInstanceProvisionPendingEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.pending(processingUnitInstanceProvisionPendingEvent);
                }
            });
        }
    }
}
