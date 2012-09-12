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
 * An aggregated view of {@link org.openspaces.admin.transport.TransportStatistics}.
 *
 * @author kimchy
 */
public interface TransportsStatistics {

    /**
     * Returns <code>true</code> if the statistics are currently not available.
     */
    boolean isNA();

    /**
     * Returns the number of {@link org.openspaces.admin.transport.TransportStatistics} that are
     * aggregated.
     */
    int getSize();

    /**
     * Returns the timestamp when this statistics was sampled.
     */
    long getTimestamp();

    /**
     * Returns the previous statistics timestamp. Returns <code>-1</code> if this is the first one.
     */
    long getPreviousTimestamp();

    /**
     * Returns the previous statistics. Returns <code>null</code> if this is the first one.
     */
    TransportsStatistics getPrevious();

    /**
     * Returns the aggregated transports details.
     */
    TransportsDetails getDetails();

    /**
     * Returns the summation of all transport {@link TransportStatistics#getCompletedTaskCount()} ()}.
     */
    long getCompletedTaskCount();

    /**
     * Returns the summation of all transport {@link TransportStatistics#getCompletedTaskPerSecond()}.
     */
    double getCompletedTaskPerSecond();

    /**
     * Returns the summation of all transport {@link TransportStatistics#getActiveThreadsCount()}.
     */
    int getActiveThreadsCount();

    /**
     * Returns the summation of all transport {@link TransportStatistics#getActiveThreadsPerc()}.
     */
    double getActiveThreadsPerc();

    /**
     * Returns the summation of all transport {@link TransportStatistics#getQueueSize()}.
     */
    int getQueueSize();
}