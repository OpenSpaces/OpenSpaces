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

import com.gigaspaces.cluster.replication.async.mirror.MirrorStatistics;
import com.j_spaces.core.filters.ReplicationStatistics;

/**
 * Returns {@link org.openspaces.admin.space.SpaceInstance} level statistics.
 *
 * @author kimchy
 */
public interface SpaceInstanceStatistics {

    /**
     * Returns <code>true</code> if this statistics are not available.
     */
    boolean isNA();

    /**
     * Returns the timestamp the statistics were taken at.
     */
    long getTimestamp();

    /**
     * Returns a timestamp that is in sync with where the admin API is running. Can return
     * -1 if the clocks have are not sync yet.
     */
    long getAdminTimestamp();

    /**
     * Returns the previous statistics timestamp, or <code>-1</code> if not available.
     */
    long getPreviousTimestamp();

    /**
     * Returns the previous statistics.
     */
    SpaceInstanceStatistics getPrevious();

    long getWriteCount();

    double getWritePerSecond();

    long getReadCount();

    double getReadPerSecond();

    long getTakeCount();

    double getTakePerSecond();

    long getNotifyRegistrationCount();

    double getNotifyRegistrationPerSecond();

    @Deprecated
    long getCleanCount();

    @Deprecated
    double getCleanPerSecond();

    long getUpdateCount();

    double getUpdatePerSecond();

    long getNotifyTriggerCount();

    double getNotifyTriggerPerSecond();

    long getNotifyAckCount();

    double getNotifyAckPerSecond();

    long getExecuteCount();

    double getExecutePerSecond();

    /**
     * Remove happens when an entry is removed due to lease expiration or lease cancel.
     */
    long getRemoveCount();

    double getRemovePerSecond();

    ReplicationStatistics getReplicationStatistics();
    
    MirrorStatistics getMirrorStatistics();
    
    /**
     * Gets the current number of pending tasks in the space processor queue.
     * @since 8.0
     */
    int getProcessorQueueSize();
    
    /**
     * Gets the current number of pending notifications that needs to be sent to different clients.
     * @since 8.0
     */
    int getNotifierQueueSize();
    
    /**
     * @since 9.1.0
     * @return count of all the objects in this Space instance.
     */
    long getObjectCount();

    /**
     * @since 9.1.0
     * @return count of all the notify templates this Space instance.
     */
    long getNotifyTemplateCount();
    
    /**
     * @since 9.1.0
     * @return count of all the active connections to this Space instance.
     */
    long getActiveConnectionCount();
    
    /**
     * @since 9.1.0
     * @return count of all the active transactions (of all types) in this Space instance.
     */
    long getActiveTransactionCount();
}
