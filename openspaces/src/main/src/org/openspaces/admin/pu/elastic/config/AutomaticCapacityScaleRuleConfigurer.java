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

import org.openspaces.admin.AdminException;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;

/**
 * Fluent API for creating a new {@link AutomaticCapacityScaleRuleConfig} object. 
 * @author itaif
 * @sine 9.0.0
 */
public class AutomaticCapacityScaleRuleConfigurer {
    
    private final AutomaticCapacityScaleRuleConfig config;
    
    public AutomaticCapacityScaleRuleConfigurer() {
        config = new AutomaticCapacityScaleRuleConfig();
    }

    /**
     * @see AutomaticCapacityScaleRuleConfig#setStatistics(ProcessingUnitStatisticsId)
     */
    public AutomaticCapacityScaleRuleConfigurer statistics(ProcessingUnitStatisticsId statisticsId) {
        config.setStatistics(statisticsId);
        return this;
    }

    /**
     * @see AutomaticCapacityScaleRuleConfig#setLowThreshold(Object)
     */
    public AutomaticCapacityScaleRuleConfigurer lowThreshold(Object lowThreshold) {
        try {
            if (lowThreshold.getClass().getConstructor(String.class) == null) {
                throw new IllegalArgumentException("lowThreshold type (" + lowThreshold.getClass() +") does not have a constructor that accepts a String");
            }
        } catch (SecurityException e) {
            throw new AdminException("Failed to verify low threshold class type",e);
        } catch (NoSuchMethodException e) {
            throw new AdminException("Failed to verify low threshold class type",e);
        }
        config.setLowThreshold(lowThreshold);
        return this;
    }
    
    /**
     * @see AutomaticCapacityScaleRuleConfig#setHighThreshold(double)
     */
    public AutomaticCapacityScaleRuleConfigurer highThreshold(Object highThreshold) {
        try {
            if (highThreshold.getClass().getConstructor(String.class) == null) {
                throw new IllegalArgumentException("highThreshold type (" + highThreshold.getClass() +") does not have a constructor that accepts a String");
            }
        } catch (SecurityException e) {
            throw new AdminException("Failed to verify low threshold class type",e);
        } catch (NoSuchMethodException e) {
            throw new AdminException("Failed to verify low threshold class type",e);
        }
        config.setHighThreshold(highThreshold);
        return this;
    }
    
    public AutomaticCapacityScaleRuleConfig create() {
        return this.config;
    }
}
