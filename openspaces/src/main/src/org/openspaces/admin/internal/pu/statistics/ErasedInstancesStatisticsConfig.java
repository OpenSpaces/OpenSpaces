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
package org.openspaces.admin.internal.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.pu.statistics.AbstractInstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.InstancesStatisticsConfig;


/**
 * Used by {@link InstancesStatisticsCalculator} to group statisticsIds that differ only by instancesStatistics
 * by erasing (ovveriding) the instancesStatistics value by {@link ErasedInstancesStatisticsConfig} 
 * @author itaif
 * @since 9.0.0
 */
public class ErasedInstancesStatisticsConfig 
    extends AbstractInstancesStatisticsConfig 
    implements InstancesStatisticsConfig {

    public ErasedInstancesStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public ErasedInstancesStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }
    
    @Override
    public void validate() throws IllegalStateException {
        // ok
    }
}
