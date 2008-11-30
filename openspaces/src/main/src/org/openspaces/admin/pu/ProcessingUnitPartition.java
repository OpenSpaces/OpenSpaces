package org.openspaces.admin.pu;

/**
 * @author kimchy
 */
public interface ProcessingUnitPartition {

    /**
     * Returns the partition id (starting from 0). Note, {@link org.openspaces.admin.space.SpaceInstance#getInstanceId()}
     * starts from 1.
     */
    int getPartitiondId();

    ProcessingUnitInstance[] getInstances();

    ProcessingUnit getProcessingUnit();
}
