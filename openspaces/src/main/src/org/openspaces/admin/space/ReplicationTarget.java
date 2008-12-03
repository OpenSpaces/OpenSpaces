package org.openspaces.admin.space;

import org.openspaces.admin.internal.space.InternalSpaceInstance;

/**
 * Represnts a replication target from one {@link org.openspaces.admin.space.SpaceInstance} to the
 * other.
 *
 * @author kimchy
 */
public class ReplicationTarget {

    private final InternalSpaceInstance spaceInstance;

    private final ReplicationStatus replicationStatus;

    public ReplicationTarget(InternalSpaceInstance spaceInstance, ReplicationStatus replicationStatus) {
        this.spaceInstance = spaceInstance;
        this.replicationStatus = replicationStatus;
    }

    /**
     * Retuns the space instnace that will be replicated to. Can be <code>null</code>
     * if replication is disabled/disconnected.
     */
    public InternalSpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    /**
     * Returns the replication status.
     */
    public ReplicationStatus getReplicationStatus() {
        return replicationStatus;
    }
}
