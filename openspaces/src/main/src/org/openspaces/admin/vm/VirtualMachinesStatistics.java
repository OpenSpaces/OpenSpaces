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

package org.openspaces.admin.vm;

/**
 * An aggregated statistics view of all the different virtual machines currently discovered.
 *
 * @author kimchy
 */
public interface VirtualMachinesStatistics {

    /**
     * Returns <code>true</code> if this is not valid statistics.
     */
    boolean isNA();

    /**
     * Return the timestamp when the statistics were taken.
     */
    long getTimestamp();

    /**
     * Returns the previous timestamp of the statistics sampled, <code>-1</code> if this is the
     * first one.
     */
    long getPreviousTimestamp();

    /**
     * Returns the previous statistics sampled. <code>null</code> if this is the first one.
     */
    VirtualMachinesStatistics getPrevious();

    /**
     * Returns the details of the all the virtual machines.
     */
    VirtualMachinesDetails getDetails();

    /**
     * Retruns the number of statistics (virtual machines) aggregated.
     */
    int getSize();

    long getUptime();

    long getMemoryHeapCommittedInBytes();
    double getMemoryHeapCommittedInMB();
    double getMemoryHeapCommittedInGB();

    long getMemoryHeapUsedInBytes();
    double getMemoryHeapUsedInMB();
    double getMemoryHeapUsedInGB();

    /**
     * Returns the memory heap percentage from used to the max.
     */
    double getMemoryHeapUsedPerc();

    /**
     * Returns the memory heap percentage from used to committed.
     */
    double getMemoryHeapCommittedUsedPerc();

    long getMemoryNonHeapCommittedInBytes();
    double getMemoryNonHeapCommittedInMB();
    double getMemoryNonHeapCommittedInGB();

    long getMemoryNonHeapUsedInBytes();
    double getMemoryNonHeapUsedInMB();
    double getMemoryNonHeapUsedInGB();

    /**
     * Returns the memory non heap percentage from used to the max.
     */
    double getMemoryNonHeapUsedPerc();

    /**
     * Returns the memory non heap percentage from used to committed.
     */
    double getMemoryNonHeapCommittedUsedPerc();

    int getThreadCount();

    int getPeakThreadCount();

    long getGcCollectionCount();

    long getGcCollectionTime();

    /**
     * The percentage of the gc collection time between the current sampled statistics
     * and the previous one.
     */
    double getGcCollectionPerc();
}