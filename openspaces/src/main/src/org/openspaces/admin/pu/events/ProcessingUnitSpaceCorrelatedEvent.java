package org.openspaces.admin.pu.events;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.Space;

/**
 * An event that indicates that an (embedded) {@link Space} was correlated with the processing unit.
 *
 * @author kimchy
 */
public class ProcessingUnitSpaceCorrelatedEvent {

    private final Space space;

    private final ProcessingUnit processingUnit;

    public ProcessingUnitSpaceCorrelatedEvent(Space space, ProcessingUnit processingUnit) {
        this.space = space;
        this.processingUnit = processingUnit;
    }

    public Space getSpace() {
        return space;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
}
