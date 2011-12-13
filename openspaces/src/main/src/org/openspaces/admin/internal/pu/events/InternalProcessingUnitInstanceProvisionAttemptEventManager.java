package org.openspaces.admin.internal.pu.events;

import org.jini.rio.monitor.ProvisionLifeCycleEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionAttemptEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionAttemptEventManager;

public interface InternalProcessingUnitInstanceProvisionAttemptEventManager extends ProcessingUnitInstanceProvisionAttemptEventManager {
    
    void raiseAttemptEvent(ProvisionLifeCycleEvent provisionEvent, ProcessingUnitInstanceProvisionAttemptEvent processingUnitInstanceProvisionAttemptEvent, DefaultProcessingUnitInstanceProvisionEventsManager processingUnitProvisionEventsManager);
}
