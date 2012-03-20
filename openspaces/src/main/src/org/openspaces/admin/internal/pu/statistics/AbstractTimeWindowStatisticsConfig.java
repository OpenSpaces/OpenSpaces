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
package org.openspaces.admin.internal.pu.statistics;

/**
 * @author itaif
 *
 */
public abstract class AbstractTimeWindowStatisticsConfig implements InternalTimeWindowStatisticsConfig {

    private long timeWindowSeconds;
    private long minimumTimeWindowSeconds;
    private long maximumTimeWindowSeconds;
    
    /**
     * @return the timeWindowSeconds
     */
    public long getTimeWindowSeconds() {
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
    public long getMinimumTimeWindowSeconds() {
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
    public long getMaximumTimeWindowSeconds() {
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
        result = prime * result + (int) (maximumTimeWindowSeconds ^ (maximumTimeWindowSeconds >>> 32));
        result = prime * result + (int) (minimumTimeWindowSeconds ^ (minimumTimeWindowSeconds >>> 32));
        result = prime * result + (int) (timeWindowSeconds ^ (timeWindowSeconds >>> 32));
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
        if (maximumTimeWindowSeconds != other.maximumTimeWindowSeconds)
            return false;
        if (minimumTimeWindowSeconds != other.minimumTimeWindowSeconds)
            return false;
        if (timeWindowSeconds != other.timeWindowSeconds)
            return false;
        return true;
    }
}
