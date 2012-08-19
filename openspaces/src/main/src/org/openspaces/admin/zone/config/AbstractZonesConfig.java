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

import org.openspaces.admin.config.AbstractConfig;

/**
 * @author elip
 * @since 9.1.0
 */
public abstract class AbstractZonesConfig 
    extends AbstractConfig 
    implements ZonesConfig {    
    
    private static final String ZONES_KEY = "zones";
    
    /**
     * @param properties
     */
    protected AbstractZonesConfig(Map<String, String> properties) {
        super(properties);
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.pu.statistics.ZoneStatisticsConfig#validate()
     */
    @Override
    public void validate() throws IllegalStateException {
        Set<String> zones = getZones();
        if (zones == null) {
            throw new IllegalStateException("zones cannot be null");
        }
        for (String zone : zones) {
            if (zone == null) {
                throw new IllegalStateException("zone cannot be null");
            }
            if (zone.isEmpty()) {
                throw new IllegalStateException("zone cannot be empty");
            }
        }
    }
    
    @Override
    public Set<String> getZones() {
        return super.getStringProperties().getSet(ZONES_KEY, ",", new HashSet<String>());
        
    }
    
    public void setZones(Set<String> zones) {
        super.getStringProperties().putSet(ZONES_KEY, zones, ",");
        validate();
    }
}
