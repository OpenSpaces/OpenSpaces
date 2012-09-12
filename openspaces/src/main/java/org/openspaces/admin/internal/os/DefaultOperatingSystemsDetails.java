/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.os;

import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.os.OperatingSystemsDetails;
import org.openspaces.admin.support.StatisticsUtils;

/**
 * @author kimchy
 */
public class DefaultOperatingSystemsDetails implements OperatingSystemsDetails {

    private final OperatingSystemDetails[] details;

    public DefaultOperatingSystemsDetails(OperatingSystemDetails[] details) {
        this.details = details;
    }

    public int getAvailableProcessors() {
        int total = 0;
        for (OperatingSystemDetails detail : details) {
            total += detail.getAvailableProcessors();
        }
        return total;
    }

    public long getTotalSwapSpaceSizeInBytes() {
        long total = 0;
        for (OperatingSystemDetails detail : details) {
            if (detail.getTotalSwapSpaceSizeInBytes() != -1) {
                total += detail.getTotalSwapSpaceSizeInBytes();
            }
        }
        return total;
    }

    public double getTotalSwapSpaceSizeInMB() {
        return StatisticsUtils.convertToMB(getTotalSwapSpaceSizeInBytes());
    }

    public double getTotalSwapSpaceSizeInGB() {
        return StatisticsUtils.convertToGB(getTotalSwapSpaceSizeInBytes());
    }

    public long getTotalPhysicalMemorySizeInBytes() {
        long total = 0;
        for (OperatingSystemDetails detail : details) {
            if (detail.getTotalPhysicalMemorySizeInBytes() != -1) {
                total += detail.getTotalPhysicalMemorySizeInBytes();
            }
        }
        return total;
    }

    public double getTotalPhysicalMemorySizeInMB() {
        return StatisticsUtils.convertToMB(getTotalPhysicalMemorySizeInBytes());
    }

    public double getTotalPhysicalMemorySizeInGB() {
        return StatisticsUtils.convertToGB(getTotalPhysicalMemorySizeInBytes());
    }
}
