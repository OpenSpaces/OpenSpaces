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
 * Operating System Details include static information (non-changeable) regarding the operating system.
 *
 * @author kimchy
 */
public interface OperatingSystemDetails {

    /**
     * Retruns <code>true</code> if the details are not available.
     */
    boolean isNA();

    /**
     * Retruns the uid of the operating system.
     */
    String getUid();

    /**
     * Returns the name of the operating system.
     *
     * @see java.lang.management.OperatingSystemMXBean#getName()
     */
    String getName();

    /**
     * Returns the architecture of the operating system.
     *
     * @see java.lang.management.OperatingSystemMXBean#getArch()
     */
    String getArch();

    /**
     * Returns the version of the operating system.
     *
     * @see java.lang.management.OperatingSystemMXBean#getVersion()
     */
    String getVersion();

    /**
     * Returns the number of available processors.
     *
     * @see java.lang.management.OperatingSystemMXBean#getAvailableProcessors()
     */
    int getAvailableProcessors();

    /**
     * Returns the total swap space size in bytes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    long getTotalSwapSpaceSizeInBytes();

    /**
     * Returns the total swap space size in mega bytes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    double getTotalSwapSpaceSizeInMB();

    /**
     * Returns the total swap space size in giga bytes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    double getTotalSwapSpaceSizeInGB();

    /**
     * Returns the total physical memory size in bytes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    long getTotalPhysicalMemorySizeInBytes();
    
    /**
     * Returns the total physical memory size in mega bytes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    double getTotalPhysicalMemorySizeInMB();

    /**
     * Returns the total physical memory size in giga byes.
     *
     * <p>Note, currently only available on SUN VM.
     */
    double getTotalPhysicalMemorySizeInGB();

    /**
     * Returns the maximum open files descriptors allowed for the operating system.
     */
    long getOpenFilesMax();
}
