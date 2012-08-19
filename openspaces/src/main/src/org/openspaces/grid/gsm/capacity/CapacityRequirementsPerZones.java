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
package org.openspaces.grid.gsm.capacity;

import java.util.HashSet;
import java.util.Set;

import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ExactZonesConfigurer;

import com.gigaspaces.internal.utils.StringUtils;
/**
 * 
 * @author Itai Frenkel
 * @since 9.1.0
 */

public class CapacityRequirementsPerZones extends AbstractCapacityRequirementsPerKey {

    public CapacityRequirementsPerZones() {
    }
        
    public Set<ExactZonesConfig> getZones() {
        Set<ExactZonesConfig> zonesList = new HashSet<ExactZonesConfig>(); 
        for (String key : super.getKeys()) {
            zonesList.add(zonesFromString(key));
        }
        return zonesList;
    }
        
    public CapacityRequirementsPerZones set(ExactZonesConfig zones, CapacityRequirements capacity) {
        return (CapacityRequirementsPerZones) super.set(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones add(
            ExactZonesConfig zones, 
            CapacityRequirements capacity) {
        
      return (CapacityRequirementsPerZones) super.add(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(
            ExactZonesConfig zones, 
            CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtract(zonesToString(zones), capacity);
    }

    public CapacityRequirementsPerZones subtractZone(
            ExactZonesConfig zones) {
        return (CapacityRequirementsPerZones) super.subtractKey(zonesToString(zones));
    }

    public CapacityRequirementsPerZones subtractOrZero(
            ExactZonesConfig zones, CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtractOrZero(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.subtract(other);
    }

    public CapacityRequirementsPerZones add(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.add(other);
    }

    public CapacityRequirements getZonesCapacity(ExactZonesConfig zones) {
        return super.getKeyCapacity(zonesToString(zones));
    }

    private String zonesToString(ExactZonesConfig zones) {
        return StringUtils.collectionToCommaDelimitedString(zones.getZones());
    }

    private ExactZonesConfig zonesFromString(String key) {
        return new ExactZonesConfigurer().addZones(StringUtils.commaDelimitedListToStringArray(key)).create();
    }
    
    public CapacityRequirements getZonesCapacityOrZero(ExactZonesConfig zones) {
        return super.getKeyCapacityOrZero(zonesToString(zones));
    }

    @Override
    protected CapacityRequirementsPerZones newZeroInstance() {
        return new CapacityRequirementsPerZones();
    }
}
