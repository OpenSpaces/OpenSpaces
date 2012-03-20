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

public abstract class AbstractInstancesStatisticsConfig implements InternalInstancesStatisticsConfig {

    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }
    
    public abstract String toString();
    
    public Class<? extends InternalProcessingUnitStatisticsCalculator> getProcessingUnitStatisticsCalculator() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
