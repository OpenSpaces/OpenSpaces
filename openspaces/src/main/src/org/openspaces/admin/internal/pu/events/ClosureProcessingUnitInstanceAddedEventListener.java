package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitInstanceAddedEventListener extends AbstractClosureEventListener implements ProcessingUnitInstanceAddedEventListener {

    public ClosureProcessingUnitInstanceAddedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
        getClosure().call(processingUnitInstance);
    }
}