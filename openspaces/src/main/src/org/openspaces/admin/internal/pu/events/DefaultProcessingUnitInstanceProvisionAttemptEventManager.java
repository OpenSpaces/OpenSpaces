package org.openspaces.admin.internal.pu.events;

import java.util.List;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionAttemptEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionAttemptEventListener;

public class DefaultProcessingUnitInstanceProvisionAttemptEventManager extends
        AbstractProcessingUnitInstanceProvisionEventManager<ProcessingUnitInstanceProvisionAttemptEventListener>
        implements InternalProcessingUnitInstanceProvisionAttemptEventManager {

    public DefaultProcessingUnitInstanceProvisionAttemptEventManager(InternalAdmin admin) {
        super(admin);
    }
    
    @Override
    public void add(ProcessingUnitInstanceProvisionAttemptEventListener listener) {
        super.add(listener);
    }
    
    @Override
    public void remove(ProcessingUnitInstanceProvisionAttemptEventListener listener) {
        super.remove(listener);
    }
    
    @Override
    public void raiseAttemptEvent(ProvisionLifeCycleEvent provisionEvent, final ProcessingUnitInstanceProvisionAttemptEvent processingUnitInstanceProvisionAttemptEvent) {
        
        List<ProcessingUnitInstanceProvisionAttemptEventListener> eventListeners = filterListenersBySequenceId(provisionEvent);
        
        for (final ProcessingUnitInstanceProvisionAttemptEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.attempt(processingUnitInstanceProvisionAttemptEvent);
                }
            });
        }
    }
}
