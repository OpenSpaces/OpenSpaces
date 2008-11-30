package org.openspaces.admin.pu;

/**
 * @author kimchy
 */
public class ProcessingUnitEvent {

    private final ProcessingUnit processingUnit;

    public ProcessingUnitEvent(ProcessingUnit processingUnit) {
        this.processingUnit = processingUnit;
    }

    public ProcessingUnit getProcessingUnit() {
        return processingUnit;
    }
}
