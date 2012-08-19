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
package org.openspaces.admin.zone.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openspaces.admin.pu.elastic.config.AbstractStatisticsConfig;

/**
 * @author elip
 * @since 9.1.0
 */
public abstract class AbstractZoneStatisticsConfig 
    extends AbstractStatisticsConfig 
    implements ZoneStatisticsConfig {    
    
    private static final String ZONES_KEY = "zones";
    
    /**
     * @param properties
     */
    protected AbstractZoneStatisticsConfig(Map<String, String> properties) {
        super(properties);
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.pu.statistics.ZoneStatisticsConfig#validate()
     */
    @Override
    public void validate() throws IllegalStateException {
        if (getZones() == null) {
            throw new IllegalStateException("zones cannot be null");
        }
    }
    
    @Override
    public Set<String> getZones() {
        return super.getStringProperties().getSet(ZONES_KEY, ",", new HashSet<String>());
        
    }
    
    public void setZones(Set<String> zones) {
        super.getStringProperties().putSet(ZONES_KEY, zones, ",");
    }
}
