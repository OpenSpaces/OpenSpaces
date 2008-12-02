package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstancesAware {

    ProcessingUnitInstance[] getProcessingUnitInstances();
}
