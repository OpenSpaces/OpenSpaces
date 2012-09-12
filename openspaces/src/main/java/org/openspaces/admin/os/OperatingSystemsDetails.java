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
 * An aggregated view of all the currently discovered {@link org.openspaces.admin.os.OperatingSystemDetails}.
 *
 * @author kimchy
 */
public interface OperatingSystemsDetails {

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getAvailableProcessors()}.
     */
    int getAvailableProcessors();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalSwapSpaceSizeInBytes()}.
     */
    long getTotalSwapSpaceSizeInBytes();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalSwapSpaceSizeInMB()}.
     */
    double getTotalSwapSpaceSizeInMB();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalSwapSpaceSizeInGB()}.
     */
    double getTotalSwapSpaceSizeInGB();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalPhysicalMemorySizeInBytes()}.
     */
    long getTotalPhysicalMemorySizeInBytes();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalPhysicalMemorySizeInMB()}.
     */
    double getTotalPhysicalMemorySizeInMB();

    /**
     * Returns the summation of all {@link OperatingSystemDetails#getTotalPhysicalMemorySizeInGB()}.
     */
    double getTotalPhysicalMemorySizeInGB();
}
