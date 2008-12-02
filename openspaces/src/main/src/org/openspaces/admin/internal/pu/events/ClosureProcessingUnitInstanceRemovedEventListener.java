package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitInstanceRemovedEventListener extends AbstractClosureEventListener implements ProcessingUnitInstanceRemovedEventListener {

    public ClosureProcessingUnitInstanceRemovedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance) {
        getClosure().call(processingUnitInstance);
    }
}