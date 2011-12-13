package org.openspaces.admin.internal.pu.events;

import java.util.List;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionSuccessEventListener;

public class DefaultProcessingUnitInstanceProvisionSuccessEventManager extends
        AbstractProcessingUnitInstanceProvisionEventManager<ProcessingUnitInstanceProvisionSuccessEventListener>
        implements InternalProcessingUnitInstanceProvisionSuccessEventManager {

    public DefaultProcessingUnitInstanceProvisionSuccessEventManager(InternalAdmin admin) {
        super(admin);
    }
    
    @Override
    public void add(ProcessingUnitInstanceProvisionSuccessEventListener listener) {
        super.add(listener);
    }
    
    @Override
    public void remove(ProcessingUnitInstanceProvisionSuccessEventListener listener) {
        super.remove(listener);
    }
    
    
    @Override
    public void raiseSuccessEvent(ProvisionLifeCycleEvent provisionEvent,
            final ProcessingUnitInstance processingUnitInstance,
            DefaultProcessingUnitInstanceProvisionEventsManager processingUnitInstanceProvisionEventsManager) {
        
        List<ProcessingUnitInstanceProvisionSuccessEventListener> eventListeners = filterListenersBySequenceId(provisionEvent, processingUnitInstanceProvisionEventsManager);
        
        for (final ProcessingUnitInstanceProvisionSuccessEventListener listener : eventListeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.success(processingUnitInstance);
                }
            });
        }
    }
}
