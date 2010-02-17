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

package org.openspaces.admin.pu;

/**
 * A Space partition mainly make sense with partitioned topologies and holds all the
 * {@link org.openspaces.admin.pu.ProcessingUnitInstance}s that form the partition.
 *
 * @author kimchy
 */
public interface ProcessingUnitPartition {

    /**
     * Returns the partition id (starting from 0). Note, {@link ProcessingUnitInstance#getInstanceId()}
     * starts from 1.
     */
    int getPartitionId();

    /**
     * Returns the processing unit instances associated with this partition.
     */
    ProcessingUnitInstance[] getInstances();

    /**
     * Returns the processing unit this partition is part of.
     */
    ProcessingUnit getProcessingUnit();

    /**
     * If the processing unit has an embedded space, will return the processing unit instance that holds a
     * the primary space instance.
     */
    ProcessingUnitInstance getPrimary();

    /**
     * If the processing unit has an embedded space, will return the processing unit instance that holds a
     * the backup space instance.
     */
    ProcessingUnitInstance getBackup();
}
