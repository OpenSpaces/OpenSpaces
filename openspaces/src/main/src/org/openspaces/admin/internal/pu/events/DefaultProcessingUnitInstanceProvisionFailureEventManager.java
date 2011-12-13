package org.openspaces.admin.internal.pu.events;

import java.util.List;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailureEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailureEventListener;

public class DefaultProcessingUnitInstanceProvisionFailureEventManager extends
        AbstractProcessingUnitInstanceProvisionEventManager<ProcessingUnitInstanceProvisionFailureEventListener>
        implements InternalProcessingUnitInstanceProvisionFailureEventManager {

    public DefaultProcessingUnitInstanceProvisionFailureEventManager(InternalAdmin admin) {
        super(admin);
    }
    
    @Override
    public void add(ProcessingUnitInstanceProvisionFailureEventListener listener) {
        super.add(listener);
    }
    
    @Override
    public void remove(ProcessingUnitInstanceProvisionFailureEventListener listener) {
        super.remove(listener);
    }
    
    @Override
    public void raiseFailureEvent(ProvisionLifeCycleEvent provisionEvent,
            final ProcessingUnitInstanceProvisionFailureEvent processingUnitInstanceProvisionFailureEvent,
            DefaultProcessingUnitInstanceProvisionEventsManager processingUnitInstanceProvisionEventsManager) {
        
        List<ProcessingUnitInstanceProvisionFailureEventListener> eventListeners = filterListenersBySequenceId(provisionEvent, processingUnitInstanceProvisionEventsManager);
        
        for (final ProcessingUnitInstanceProvisionFailureEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.failure(processingUnitInstanceProvisionFailureEvent);
                }
            });
        }
    }
}
