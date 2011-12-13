package org.openspaces.admin.internal.pu.events;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailureEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionFailureEventManager;

public interface InternalProcessingUnitInstanceProvisionFailureEventManager extends ProcessingUnitInstanceProvisionFailureEventManager {
    
    void raiseFailureEvent(ProvisionLifeCycleEvent provisionEvent, ProcessingUnitInstanceProvisionFailureEvent processingUnitInstanceProvisionFailureEvent, DefaultProcessingUnitInstanceProvisionEventsManager processingUnitInstanceProvisionEventsManager);
}
