package org.openspaces.admin.space;

/**
 * @author kimchy
 */
public interface SpacePartition extends Iterable<SpaceInstance> {

    /**
     * Returns the partition id (starting from 0). Note, {@link SpaceInstance#getInstanceId()}
     * starts from 1.
     */
    int getPartitiondId();

    SpaceInstance[] getInstances();

    Space getSpace();

    /**
     * Returns the primary space instnace, <code>null</code> if currently there is no primary.
     */
    SpaceInstance getPrimary();

    /**
     * Returns the (first) backup space instance, <code>null</code> if currently there is no backup.
     */
    SpaceInstance getBackup();
}
