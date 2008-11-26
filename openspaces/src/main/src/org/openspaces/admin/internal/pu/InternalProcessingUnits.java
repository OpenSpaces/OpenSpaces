package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

/**
 * @author kimchy
 */
public interface InternalProcessingUnits extends ProcessingUnits {

    void addProcessingUnit(ProcessingUnit processingUnit);

    void removeProcessingUnit(String name);
}
