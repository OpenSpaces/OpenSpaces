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

import org.openspaces.core.util.MemoryUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirement;

/**
 * An interface that defines fluent API methods required to define scale strategy capacity
 * @author itaif
 * @since 9.0.0
 * @see CapacityRequirement
 * @see CapacityRequirementsConfig
 * @see CapacityRequirementsConfigurer
 */
public interface ScaleStrategyCapacityRequirementConfigurer {

    /**
     * Specifies the memory capacity (RAM).
     * @param size
     *            - the RAM size as a string (For example :50m" or "50g" or "50t")
     *            See also {@link MemoryUnit#getPostfix())
     */
    ScaleStrategyCapacityRequirementConfigurer memoryCapacity(String memory);

    /**
     * Specifies the memory capacity (RAM).
     */
    ScaleStrategyCapacityRequirementConfigurer memoryCapacity(int memory, MemoryUnit unit);
   
    /**
     * Specifies the number of CPU cores (as reported by the operating system)
     * This includes both real cores and hyper-threaded cores. 
     */
    ScaleStrategyCapacityRequirementConfigurer numberOfCpuCores(double cpuCores);

    /**
     * Specifies the disk and network drive capacity.
     * 
     * @param drive
     *            - the file system directory representing the drive
     * @param size
     *            - the drive size
     * @param unit
     *            - the drive size memory unit.
     * 
     *            For example: driveCapacity("/",50,MemoryUnit.MEGABYTES) - the drive "/" (on linux)
     *            has the size of 50*1024MBs driveCapacity("c:\\",50,MemoryUnit.MEGABYTES) - the
     *            drive "c:\" (on windows) has the size of 50*1024MBs
     */
    ScaleStrategyCapacityRequirementConfigurer driveCapacity(String drive, int size, MemoryUnit unit);
    
    /**
     * Specifies the disk and network drive capacity.
     * 
     * @param drive
     *            - the file system directory representing the drive
     * @param size
     *            - the drive size as a string (For example :50m" or "50g" or "50t")
     *            See also {@link MemoryUnit#getPostfix())
     * 
     *            For example: 
     *            driveCapacity("/","50m") - the drive "/" (on linux) has the size of 50*1024MBs 
     *            driveCapacity("c:\\","50m") - the drive "c:\" (on windows) has the size of 50*1024MBs
     */
    ScaleStrategyCapacityRequirementConfigurer driveCapacity(String drive, String size);
}
