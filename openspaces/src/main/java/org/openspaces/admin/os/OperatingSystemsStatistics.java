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

package org.openspaces.admin.os;

import java.util.List;

/**
 * An aggregated statistics of all the different {@link org.openspaces.admin.os.OperatingSystemStatistics}.
 *
 * @author kimchy
 */
public interface OperatingSystemsStatistics {

    /**
     * Returns <code>true</code> if the statistics are not available.
     */
    boolean isNA();

    /**
     * Returns the timestamp when the aggregated statistics was computed.
     */
    long getTimestamp();

    /**
     * Returns the number of aggregated {@link org.openspaces.admin.os.OperatingSystemStatistics}.
     */
    int getSize();

    /**
     * Returns the previous {@link org.openspaces.admin.os.OperatingSystemsStatistics}.
     */
    OperatingSystemsStatistics getPrevious();

    /**
     * Returns the timeline (from newest to oldest) history statistics, including this one.
     */
    List<OperatingSystemsStatistics> getTimeline();

    /**
     * Returns the aggregated operating systems details.
     */
    OperatingSystemsDetails getDetails();


    long getFreeSwapSpaceSizeInBytes();

    double getFreeSwapSpaceSizeInMB();

    double getFreeSwapSpaceSizeInGB();

    long getFreePhysicalMemorySizeInBytes();

    double getFreePhysicalMemorySizeInMB();

    double getFreePhysicalMemorySizeInGB();

    long getActualFreePhysicalMemorySizeInBytes();

    double getActualFreePhysicalMemorySizeInMB();

    double getActualFreePhysicalMemorySizeInGB();


    /**
     * Returns the combined cpu perc (User + Sys + Nice + Wait)
     */
    double getCpuPerc();

    String getCpuPercFormatted();
    
    double getMinCpuPerc();
    
    String getMinCpuPercFormatted();
    
    double getMaxCpuPerc();

    String getMaxCpuPercFormatted();
    
    double getMemoryUsedPerc();
    String getMemoryUsedPercFormatted();
    
    double getMinMemoryUsedPerc();
    String getMinMemoryUsedPercFormatted();
    
    double getMaxMemoryUsedPerc();
    String getMaxMemoryUsedPercFormatted();
}