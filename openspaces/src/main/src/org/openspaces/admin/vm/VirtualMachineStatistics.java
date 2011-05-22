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
import java.util.concurrent.TimeUnit;

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

    /**
     * Returns a timestamp that is in sync with where the admin API is running. Can return
     * -1 if the clocks have are not sync yet.
     */
    long getAdminTimestamp();

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

    /**
     * Returns the number of currently running threads in this virtual machine 
     */
    int getThreadCount();

    /**
     * Returns the maximum number of of threads that were running in the VM since it has started
     */
    int getPeakThreadCount();

    /**
     * Returns the total number of times GC was invoked for this virtual machine
     */
    long getGcCollectionCount();

    /**
     * Returns the total time in milliseconds that this virtual machine spent doing GC  
     */
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

    /**
     * Calculates the average number of cpu cores used by the java virtual machine over the specified period of time.
     * 
     * If not enough statistics have been accumulated, -1 is returned. 
     * In that case either call this method again after enough time has elapsed, or increase
     * history size {@link org.openspaces.admin.Admin#setStatisticsHistorySize(int)}
     * or increase statistics interval {@link org.openspaces.admin.Admin#setStatisticsInterval(long, TimeUnit)}.
     * 
     * @param requestedTotalTime The period of time to average the cpu percentage starting with this statistics
     * @param timeUnit Time units for totalTime
     * @return A value between 0 and number of CPU cores representing the average number of CPU cores used, or -1 if not enough statistics have been accumulated. 
     */
    public double getCpuPercAverage(long requestedTotalTime, TimeUnit timeUnit);
    
}
