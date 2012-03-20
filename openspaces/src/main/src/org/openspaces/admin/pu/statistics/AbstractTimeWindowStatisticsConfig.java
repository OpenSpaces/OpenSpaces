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
package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.InternalTimeWindowStatisticsConfig;

/**
 * Base class for statistics configurations that aggregate samples based on a specified time window
 * @author itaif
 * @since 9.0.0
 */
public abstract class AbstractTimeWindowStatisticsConfig implements InternalTimeWindowStatisticsConfig {

    private Long timeWindowSeconds;
    private Long minimumTimeWindowSeconds;
    private Long maximumTimeWindowSeconds;
    
    /**
     * @return the timeWindowSeconds
     */
    public Long getTimeWindowSeconds() {
        return timeWindowSeconds;
    }
    /**
     * @param timeWindowSeconds the timeWindowSeconds to set
     */
    public void setTimeWindowSeconds(long timeWindowSeconds) {
        this.timeWindowSeconds = timeWindowSeconds;
    }
    /**
     * @return the minimumTimeWindowSeconds
     */
    public Long getMinimumTimeWindowSeconds() {
        return minimumTimeWindowSeconds;
    }
    /**
     * @param minimumTimeWindowSeconds the minimumTimeWindowSeconds to set
     */
    public void setMinimumTimeWindowSeconds(long minimumTimeWindowSeconds) {
        this.minimumTimeWindowSeconds = minimumTimeWindowSeconds;
    }
    /**
     * @return the maximumTimeWindowSeconds
     */
    public Long getMaximumTimeWindowSeconds() {
        return maximumTimeWindowSeconds;
    }
    /**
     * @param maximumTimeWindowSeconds the maximumTimeWindowSeconds to set
     */
    public void setMaximumTimeWindowSeconds(long maximumTimeWindowSeconds) {
        this.maximumTimeWindowSeconds = maximumTimeWindowSeconds;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((maximumTimeWindowSeconds == null) ? 0 : maximumTimeWindowSeconds.hashCode());
        result = prime * result + ((minimumTimeWindowSeconds == null) ? 0 : minimumTimeWindowSeconds.hashCode());
        result = prime * result + ((timeWindowSeconds == null) ? 0 : timeWindowSeconds.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractTimeWindowStatisticsConfig other = (AbstractTimeWindowStatisticsConfig) obj;
        if (maximumTimeWindowSeconds == null) {
            if (other.maximumTimeWindowSeconds != null)
                return false;
        } else if (!maximumTimeWindowSeconds.equals(other.maximumTimeWindowSeconds))
            return false;
        if (minimumTimeWindowSeconds == null) {
            if (other.minimumTimeWindowSeconds != null)
                return false;
        } else if (!minimumTimeWindowSeconds.equals(other.minimumTimeWindowSeconds))
            return false;
        if (timeWindowSeconds == null) {
            if (other.timeWindowSeconds != null)
                return false;
        } else if (!timeWindowSeconds.equals(other.timeWindowSeconds))
            return false;
        return true;
    }
    
    public void validate() throws IllegalStateException {
        if (timeWindowSeconds == null) {
            throw new IllegalStateException("timeWindowSeconds cannot be null");
        }
        
        if (timeWindowSeconds<=0) {
            throw new IllegalStateException("timeWindowSeconds must be positive");
        }
        
        if (minimumTimeWindowSeconds == null) {
            throw new IllegalStateException("minimumTimeWindowSeconds cannot be null");
        }
        
        if (minimumTimeWindowSeconds <0) {
            throw new IllegalStateException("minimumTimeWindowSeconds must not be negative");
        }
        
        if (maximumTimeWindowSeconds == null) {
            throw new IllegalStateException("maximumTimeWindowSeconds cannot be null");
        }
        
        if (maximumTimeWindowSeconds <=0) {
            throw new IllegalStateException("maximumTimeWindowSeconds must be positive");
        }
        
        if (maximumTimeWindowSeconds < timeWindowSeconds) {
            throw new IllegalStateException("maximumTimeWindowSeconds must be bigger or equals timeWindowSeconds");
        }
        
        if (minimumTimeWindowSeconds > timeWindowSeconds) {
            throw new IllegalStateException("minimumTimeWindowSeconds must be less or equals timeWindowSeconds");
        }
    }
    
    public abstract String toString();
}
