package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.support.AbstractClosureEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventListener;

/**
 * @author kimchy
 */
public class ClosureProcessingUnitInstanceStatisticsChangedEventListener extends AbstractClosureEventListener implements ProcessingUnitInstanceStatisticsChangedEventListener {

    public ClosureProcessingUnitInstanceStatisticsChangedEventListener(Object closure) {
        super(closure);
    }

    public void processingUnitInstanceStatisticsChanged(ProcessingUnitInstanceStatisticsChangedEvent event) {
        getClosure().call(event);
    }
}