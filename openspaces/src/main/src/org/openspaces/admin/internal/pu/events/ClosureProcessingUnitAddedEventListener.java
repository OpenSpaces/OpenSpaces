package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitAddedEventListener extends AbstractClosureEventListener implements ProcessingUnitAddedEventListener {

    public ClosureProcessingUnitAddedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitAdded(ProcessingUnit processingUnit) {
        getClosure().call(processingUnit);
    }
}