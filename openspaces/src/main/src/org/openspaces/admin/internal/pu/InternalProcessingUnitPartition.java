package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitPartition extends ProcessingUnitPartition, Iterable<ProcessingUnitInstance> {

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);
}
