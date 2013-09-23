package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * An event listener allowing to listen for orphaned {@link org.openspaces.admin.pu.ProcessingUnitInstance} removals.
 * 
 * @author itaif
 * @since 9.7.0
 */
public interface InternalOrphanProcessingUnitInstanceLifecycleEventListener {

	/**
     * A callback indicating that an Orphaned Processing Unit Instance was removed.
     * Either the pu was discovered or the instance was removed.
     */
    void orphanProcessingUnitInstanceRemoved(ProcessingUnitInstance processingUnitInstance);
    
    /**
     * A callback indicating that an Orphaned Processing Unit Instance was added.
     * It means that the pu instance was discovered, but the pu was not.
     */
    void orphanProcessingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance);
}
