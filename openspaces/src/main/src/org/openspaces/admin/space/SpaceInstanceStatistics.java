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
