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

import org.openspaces.admin.pu.statistics.AbstractTimeWindowStatisticsConfig;


/**
 * A placeholder for an undefined time window statistics configuration that 
 * respects window size in its {@link #hashCode()} and {@link #equals(Object)} method.
 * @author itaif
 * @since 9.0.0
 * @see TimeWindowStatisticsCalculator for usage
 */
public class ErasedTimeWindowStatisticsConfig extends AbstractTimeWindowStatisticsConfig {

    /**
     * @param timeWindowStatistics
     */
    public ErasedTimeWindowStatisticsConfig(AbstractTimeWindowStatisticsConfig timeWindowStatistics) {
        this.setMaximumTimeWindowSeconds(timeWindowStatistics.getMaximumTimeWindowSeconds());
        this.setMinimumTimeWindowSeconds(timeWindowStatistics.getMinimumTimeWindowSeconds());
        this.setTimeWindowSeconds(timeWindowStatistics.getTimeWindowSeconds());
    }

    @Override
    public Class<? extends InternalProcessingUnitStatisticsCalculator> getProcessingUnitStatisticsCalculator() {
        return DoNothingProcessingUnitStatisticsCalculator.class;
    }

    @Override
    public String toString() {
        return "erasedTimeWindowStatistics {timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }

}
