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
package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;



/**
 * Configurations that aggregate statistics from a time window by picking up the sample with the highest value.
 * @author itaif
 * @since 9.0.0
 * @see MinimumTimeWindowStatisticsConfig
 * @see PercentileTimeWindowStatisticsConfig
 */
public class MaximumTimeWindowStatisticsConfig  
            extends AbstractTimeWindowStatisticsConfig 
            implements StatisticsObjectListFunction {

    public MaximumTimeWindowStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public MaximumTimeWindowStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }


    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getMaximum();
    }

}