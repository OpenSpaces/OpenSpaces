/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import java.util.Map;

import org.openspaces.grid.gsm.capacity.CapacityRequirements;

/**
 * Defines a POJO that describes memory,cpu cores,drive capacity
 * Can be converted into a {@link CapacityRequirements} with {#toCapacityRequirements()}.
 * @author itaif
 * @since 9.0.0
 */
public interface ScaleStrategyCapacityRequirementConfig {

    /**
     * Specifies the total memory capacity.
     */
    void setMemoryCapacityInMB(long memory);

    long getMemoryCapacityInMB() throws NumberFormatException;
    
    /**
     * Specifies the total CPU cores.
     */
    public void setNumberOfCpuCores(double cpuCores);

    double getNumberOfCpuCores();

    /**
     * Specifies the disk and network drive capacity.
     * @param megaBytesPerDrive - a mapping between the file system directory representing the drive and its capacity (in mega-bytes) needed by the pu .
     * 
     * For example the drive "/" (on linux) has the size of 50*1024MBs
     * or the drive "c:\" (on windows)  has the size of 50*1024MBs
     * 
     * @since 8.0.3
     */
    void setDrivesCapacityInMB(Map<String,Long> megaBytesPerDrive);
    
    Map<String,Long> getDrivesCapacityInMB() throws NumberFormatException;
    
    CapacityRequirements toCapacityRequirements();
}
