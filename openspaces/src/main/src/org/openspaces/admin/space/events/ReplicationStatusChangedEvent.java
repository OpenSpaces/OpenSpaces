/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.space.events;

import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;

/**
 * An event indicating that the replication status was changed from a {@link SpaceInstance} to
 * its {@link ReplicationTarget}.
 *
 * @author kimchy
 * @see org.openspaces.admin.space.SpaceInstance#getReplicationStatusChanged()
 * @see org.openspaces.admin.space.Space#getReplicationStatusChanged()
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

    /**
     * The Space Instance that replicates to a replication target.
     */
    public SpaceInstance getSpaceInstance() {
        return spaceInstance;
    }

    /**
     * The replication target the Space Instance is replicating to.
     */
    public ReplicationTarget getReplicationTarget() {
        return replicationTarget;
    }

    /**
     * The previous replication status.
     */
    public ReplicationStatus getPreviousStatus() {
        return previousStatus;
    }

    /**
     * The new replication status.
     */
    public ReplicationStatus getNewStatus() {
        return newStatus;
    }
}