package org.openspaces.admin.space.events;

import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;

/**
 * @author kimchy   
 */
public class ReplicationStatusChangedEvent {

    private final SpaceInstance spaceInstance;

    private final ReplicationTarget replicationTarget;

    private final ReplicationStatus previousStatus;

    private final ReplicationStatus newStatus;

    public ReplicationStatusChangedEvent(SpaceInstance spaceInstance, ReplicationTarget replicationTarget, ReplicationStatus previousStatus, ReplicationStatus newStatus) {
        this.spaceInstance = spaceInstance;
        this.replicationTarget = replicationTarget;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    public ReplicationTarget getReplicationTarget() {
        return replicationTarget;
    }

    public ReplicationStatus getPreviousStatus() {
        return previousStatus;
    }

    public ReplicationStatus getNewStatus() {
        return newStatus;
    }
}