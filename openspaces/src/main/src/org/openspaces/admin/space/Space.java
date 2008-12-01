package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface Space extends Iterable<SpaceInstance> {

    String getUid();

    String getName();

    int getNumberOfInstances();

    int getNumberOfBackups();

    SpaceInstance[] getInstnaces();

    SpacePartition[] getPartitions();

    SpacePartition getPartition(int partitionId);

    int getSize();

    boolean isEmpty();
}

