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

import java.util.concurrent.TimeUnit;


/**
 * Provides fluent API for creating a new {@link AutomaticCapacityScaleConfig} object.
 * 
 * @author itaif
 * @since 9.0
 * @see AutomaticCapacityScaleConfig
 */
public class AutomaticCapacityScaleConfigurer implements ScaleStrategyConfigurer<AutomaticCapacityScaleConfig>{

    private final AutomaticCapacityScaleConfig config;
    
    /**
     * Provides fluent API for creating a new {@link AutomaticCapacityScaleConfig} object.
     */
    public AutomaticCapacityScaleConfigurer() {
        this.config = new AutomaticCapacityScaleConfig();
    }

    @Override
    public AutomaticCapacityScaleConfigurer maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        config.setMaxConcurrentRelocationsPerMachine(maxNumberOfConcurrentRelocationsPerMachine);
        return this;
    }

    @Override
    public AutomaticCapacityScaleConfigurer atMostOneContainerPerMachine() {
        config.setAtMostOneContainerPerMachine(true);
        return this;
    }

    @Override
    public AutomaticCapacityScaleConfigurer pollingInterval(long pollingInterval, TimeUnit timeUnit) {
        config.setPollingIntervalSeconds((int) timeUnit.toSeconds(pollingInterval));
        return this;
    }
    
    public AutomaticCapacityScaleConfigurer cooldownAfterInstanceAdded(long cooldown, TimeUnit timeUnit) {
        config.setCooldownAfterInstanceAddedSeconds(timeUnit.toSeconds(cooldown));
        return this;
    }
    
    public AutomaticCapacityScaleConfigurer cooldownAfterInstanceRemoved(long cooldown, TimeUnit timeUnit) {
        config.setCooldownAfterInstanceRemovedSeconds(timeUnit.toSeconds(cooldown));
        return this;
    }
    
    public AutomaticCapacityScaleConfigurer statisticsPollingInterval(long statisticsPollingInterval, TimeUnit timeUnit) {
        config.setStatisticsPollingIntervalSeconds((int) timeUnit.toSeconds(statisticsPollingInterval));
        return this;
    }
    
    public AutomaticCapacityScaleConfigurer minCapacity(CapacityRequirementConfig minCapacity) {
        config.setMinCapacity(minCapacity);
        return this;
    }
    
    public AutomaticCapacityScaleConfigurer maxCapacity(CapacityRequirementConfig maxCapacity) {
        config.setMaxCapacity(maxCapacity);
        return this;
    }
    
    public AutomaticCapacityScaleConfig create() {
        return config;
    }
}
