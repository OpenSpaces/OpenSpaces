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

import org.openspaces.admin.zone.config.AbstractZoneStatisticsConfig;
import org.openspaces.admin.zone.config.ZoneStatisticsConfig;

/**
 * @author elip
 *
 */
public class ErasedZonesStatisticsConfig extends AbstractZoneStatisticsConfig {
    
    public ErasedZonesStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }

    public ErasedZonesStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    @Override
    public void validate() throws IllegalStateException {
        
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.pu.statistics.ZoneStatisticsConfig#satisfiedBy(org.openspaces.admin.pu.statistics.ZoneStatisticsConfig)
     */
    @Override
    public boolean satisfiedBy(ZoneStatisticsConfig zoneStatisticsConfig) {
        return true;
    }
}
