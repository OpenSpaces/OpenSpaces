package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitStatusChangedEventListener extends AbstractClosureEventListener implements ProcessingUnitStatusChangedEventListener {

    public ClosureProcessingUnitStatusChangedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitStatusChanged(ProcessingUnitStatusChangedEvent event) {
        getClosure().call(event);
    }
}