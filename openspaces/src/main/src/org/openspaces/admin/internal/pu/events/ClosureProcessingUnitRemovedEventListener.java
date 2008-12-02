package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitRemovedEventListener extends AbstractClosureEventListener implements ProcessingUnitRemovedEventListener {

    public ClosureProcessingUnitRemovedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitRemoved(ProcessingUnit processingUnit) {
        getClosure().call(processingUnit);
    }
}