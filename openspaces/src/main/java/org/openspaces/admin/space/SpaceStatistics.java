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
 * An aggregated statistics of all the currently discovered {@link org.openspaces.admin.space.SpaceInstance}s.
 *
 * @author kimchy
 */
public interface SpaceStatistics {

    /**
     * Returns <code>true</code> if the statistics are not available.
     */
    boolean isNA();

    /**
     * Returns the number of {@link org.openspaces.admin.space.SpaceInstanceStatistics} that are being aggregated.
     */
    int getSize();

    /**
     * Returns the previous statistics, <code>null</code> if not available.
     */
    SpaceStatistics getPrevious();

    /**
     * Returns the timestamp this space statistics were taken.
     */
    long getTimestamp();

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