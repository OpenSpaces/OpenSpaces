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
package org.openspaces.admin.pu.elastic.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.util.MemoryUnit;

/**
 * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
 * 
 * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().memoryCapacity("1500m").create()}
 * 
 * @author itaif
 * @since 8.0
 * @see ManualCapacityScaleConfig
 */
public class ManualCapacityScaleConfigurer implements ScaleStrategyConfigurer<ManualCapacityScaleConfig>{

    private final ManualCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link ManualCapacityScaleConfig} object.
     * For example {@code new ManualMemoryCapacityScaleStrategyConfigurer().memoryCapacity("1500m").create()}
     * The default constructor wraps an empty {@link ManualCapacityScaleConfig} object
     */
    public ManualCapacityScaleConfigurer() {
        this.config = new ManualCapacityScaleConfig();
    }
    
    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public ManualCapacityScaleConfigurer memoryCapacity(String memory) {
        config.setMemoryCapacityInMB(MemoryUnit.toMegaBytes(memory));
        return this;
    }

    /**
     * @see ManualCapacityScaleConfig#setMemoryCapacityInMB(int)
     */
    public ManualCapacityScaleConfigurer memoryCapacity(int memory, MemoryUnit unit) {
        config.setMemoryCapacityInMB(unit.toMegaBytes(memory));
        return this;
    }
   
    /**
     * @see ManualCapacityScaleConfig#setNumberOfCpuCores(double) 
     */
    public ManualCapacityScaleConfigurer numberOfCpuCores(double cpuCores) {
        config.setNumberOfCpuCores(cpuCores);
        return this;
    }
    
    /**
     * @see ManualCapacityScaleConfig#setDrivesCapacityInMB(String,long) 
     * @since 8.0.2
     */
    public ManualCapacityScaleConfigurer driveCapacity(String drive, int size, MemoryUnit unit) {
        setDriveCapacity(drive, unit.toMegaBytes(size));
        return this;
    }
    
    /**
     * @see ManualCapacityScaleConfig#setDrivesCapacityInMB(String,long)
     * @since 8.0.2
     */
    public ManualCapacityScaleConfigurer driveCapacity(String drive, String size) {
        setDriveCapacity(drive, MemoryUnit.toMegaBytes(size));
        return this;
    }

    private void setDriveCapacity(String drive, long sizeInMB) {
        final Map<String, Long> drivesCapacityInMB = config.getDrivesCapacityInMB();
        drivesCapacityInMB.put(drive, sizeInMB);
        config.setDrivesCapacityInMB(drivesCapacityInMB);
    }

    @Override
    public ManualCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }

    @Override
    public ManualCapacityScaleConfigurer atMostOneContainerPerMachine() {
        config.setAtMostOneContainerPerMachine(true);
        return this;
    }

    @Override
    public ManualCapacityScaleConfigurer pollingInterval(long pollingInterval, TimeUnit timeUnit) {
        config.setPollingIntervalSeconds((int)timeUnit.toSeconds(pollingInterval));
        return this;
    }
    public ManualCapacityScaleConfig create() {
        return config;
    }
}
