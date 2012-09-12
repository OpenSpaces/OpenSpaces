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

/**
 * A Space partition mainly make sense with partitioned topologies and holds all the
 * {@link SpaceInstance}s that form the partition.
 *
 * @author kimchy
 */
public interface SpacePartition extends Iterable<SpaceInstance> {

    /**
     * Returns the partition id (starting from 0). Note, {@link SpaceInstance#getInstanceId()}
     * starts from 1.
     */
    int getPartitionId();

    /**
     * Returns all the space instances that form the partition.
     */
    SpaceInstance[] getInstances();

    /**
     * Returns the Space this Space Partition is part of.
     */
    Space getSpace();

    /**
     * Returns the primary space instance, <code>null</code> if currently there is no primary.
     */
    SpaceInstance getPrimary();

    /**
     * Returns the (first) backup space instance, <code>null</code> if currently there is no backup.
     */
    SpaceInstance getBackup();
}
