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
     * Returns the number of {@link org.openspaces.admin.space.SpaceInstanceStatistics} that are being aggreagted.
     */
    int getSize();

    /**
     * Retruns the previous statstics, <code>null</code> if not available.
     */
    SpaceStatistics getPrevious();

    /**
     * Retruns the timestamp this space statistics were taken.
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

    long getCleanCount();

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
}