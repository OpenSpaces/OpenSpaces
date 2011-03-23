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

package org.openspaces.admin.space;

import org.openspaces.admin.internal.space.InternalSpaceInstance;

/**
 * Represents a replication target from one {@link org.openspaces.admin.space.SpaceInstance} to the
 * other.
 *
 * @author kimchy
 */
public class ReplicationTarget {

    private final InternalSpaceInstance spaceInstance;

    private final ReplicationStatus replicationStatus;
    
    private final String memberName;

    private final boolean isMirror;

    public ReplicationTarget(InternalSpaceInstance spaceInstance, ReplicationStatus replicationStatus, String memberName, boolean isMirror) {
        this.spaceInstance = spaceInstance;
        this.replicationStatus = replicationStatus;
        this.memberName = memberName;
        this.isMirror = isMirror;
    }

    /**
     * Returns the space instance that will be replicated to. Can be <code>null</code>
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
    
    /**
     * Returns the member name of this target.
     */
    public String getMemberName() {
        return memberName;
    }
    
    /**
     * @return true if target is a Mirror Space.
     */
    public boolean isMirror() {
        return isMirror;
    }
}
