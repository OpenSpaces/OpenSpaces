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

import org.openspaces.core.util.MemoryUnit;

/**
 * Fluent API for creating a new {@CapacityRequirementConfig} object. 
 * @author itaif
 * @since 9.0.0
 */
public class CapacityRequirementConfigurer implements ScaleStrategyCapacityRequirementConfigurer {

    private CapacityRequirementConfig config;

    public CapacityRequirementConfigurer() {
        this.config = new CapacityRequirementConfig();
    }
    @Override
    public CapacityRequirementConfigurer memoryCapacity(String memory) {
        config.setMemoryCapacityInMB(MemoryUnit.toMegaBytes(memory));
        return this;
    }

    @Override
    public CapacityRequirementConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        config.setMemoryCapacityInMB(unit.toMegaBytes(memory));
        return this;
    }
   
    @Override
    public CapacityRequirementConfigurer numberOfCpuCores(double cpuCores) {
        config.setNumberOfCpuCores(cpuCores);
        return this;
    }
    
    @Override
    public CapacityRequirementConfigurer driveCapacity(String drive, int size, MemoryUnit unit) {
        setDriveCapacity(drive, unit.toMegaBytes(size));
        return this;
    }
    
    @Override
    public CapacityRequirementConfigurer driveCapacity(String drive, String size) {
        setDriveCapacity(drive, MemoryUnit.toMegaBytes(size));
        return this;
    }

    private void setDriveCapacity(String drive, long sizeInMB) {
        final Map<String, Long> drivesCapacityInMB = config.getDrivesCapacityInMB();
        drivesCapacityInMB.put(drive, sizeInMB);
        config.setDrivesCapacityInMB(drivesCapacityInMB);
    }

}
