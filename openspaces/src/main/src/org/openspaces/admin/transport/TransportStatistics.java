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

package org.openspaces.admin.transport;

/**
 * A transport level statistics.
 *
 * @author kimchy
 */
public interface TransportStatistics {

    /**
     * Return <code>true</code> if the statistics are unavailable.
     */
    boolean isNA();

    /**
     * Returns the timestamp when the statistics were taken.
     */
    long getTimestamp();

    /**
     * Returns the previous statistics timestamp. Returns <code>-1</code> if this is the first one.
     */
    long getPreviousTimestamp();

    /**
     * Returns the transport details.
     */
    TransportDetails getDetails();

    /**
     * Returns the previous statistics sampled. Returns <code>null</code> if this is the first one.
     */
    TransportStatistics getPrevious();

    /**
     * Returns the number of communication level tasks that were completed.
     */
    long getCompletedTaskCount();

    /**
     * Returns the number of completed communication level tasks per second.
     */
    double getCompletedTaskPerSecond();

    /**
     * Returns the number of active threads currently processing a communication task.
     */
    int getActiveThreadsCount();

    /**
     * Returns the percentage of active threads out of the maximum threads.
     */
    double getActiveThreadsPerc();

    /**
     * Returns the number of currently waiting communication tasks to be executed.
     */
    int getQueueSize();
}
