package org.openspaces.admin.internal.pu;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @since 9.7.0
 * @author itaif
 *
 */
public interface InternalOrphanProcessingUnitInstancesAware {

	ProcessingUnitInstance[] getOrphanProcessingUnitInstances();
}
