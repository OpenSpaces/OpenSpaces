package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstances {

    void addOrphaned(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeOrphaned(String uid);

    void addInstance(ProcessingUnitInstance processingUnitInstance);

    ProcessingUnitInstance removeInstnace(String uid);

    ProcessingUnitInstance[] getOrphaned();
}
