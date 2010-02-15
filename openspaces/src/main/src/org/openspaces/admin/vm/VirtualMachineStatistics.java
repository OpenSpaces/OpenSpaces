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

import java.util.List;

/**
 * Statistics on a single virtual machine level.
 *
 * @author kimchy
 */
public interface VirtualMachineStatistics {

    /**
     * Returns <code>true</code> if this is not valid statistics.
     */
    boolean isNA();

    /**
     * Returns the details of the virtual machine.
     */
    VirtualMachineDetails getDetails();

    /**
     * Returns the timeline (from newest to oldest) history statistics, including this one.
     */
    List<VirtualMachineStatistics> getTimeline();

    /**
     * Returns the previous statistics sampled. <code>null</code> if this is the first one or bounded by the history size.
     */
    VirtualMachineStatistics getPrevious();

    /**
     * Returns the previous timestamp of the statistics sampled, <code>-1</code> if this is the
     * first one.
     */
    long getPreviousTimestamp();

    /**
     * Return the timestamp when the statistics were taken.
     */
    long getTimestamp();

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
     * and the previous one. If there is no previous one, will return -1.
     */
    double getGcCollectionPerc();

    /**
     * Returns the cpu percentage this virtual machine is using.
     */
    double getCpuPerc();

    String getCpuPercFormatted();
}
