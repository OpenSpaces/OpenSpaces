package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitSpaceCorrelatedEventListener extends AbstractClosureEventListener implements ProcessingUnitSpaceCorrelatedEventListener {

    public ClosureProcessingUnitSpaceCorrelatedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitSpaceCorrelated(ProcessingUnitSpaceCorrelatedEvent event) {
        getClosure().call(event);
    }
}