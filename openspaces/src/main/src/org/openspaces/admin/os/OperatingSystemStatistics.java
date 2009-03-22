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

/**
 * Statistics of a specific {@link org.openspaces.admin.os.OperatingSystem}.
 *
 * @author kimchy
 */
public interface OperatingSystemStatistics {

    /**
     * Returns <code>true</code> if the statistics are not available.
     */
    boolean isNA();

    /**
     * Returns the timestamp when the statistics were take.
     */
    long getTimestamp();

    /**
     * Returns the operating system details.
     */
    OperatingSystemDetails getDetails();

    /**
     * Returns the previous statistics taken. Returns <code>null</code> if this is the fist one.
     */
    OperatingSystemStatistics getPrevious();

    long getFreeSwapSpaceSizeInBytes();

    double getFreeSwapSpaceSizeInMB();

    double getFreeSwapSpaceSizeInGB();

    long getFreePhysicalMemorySizeInBytes();

    double getFreePhysicalMemorySizeInMB();

    double getFreePhysicalMemorySizeInGB();

    double getSystemLoadAverage();

    /**
     * Returns the combined cpu perc (User + Sys + Nice + Wait)
     */
    double getCpuPerc();

    String getCpuPercFormatted();
}
