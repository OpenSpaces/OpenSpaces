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

import static org.openspaces.admin.internal.zone.config.ZonesConfigUtils.zonesFromString;
import static org.openspaces.admin.internal.zone.config.ZonesConfigUtils.zonesToString;

import java.util.HashSet;
import java.util.Set;

import org.openspaces.admin.zone.config.ZonesConfig;

/**
 * 
 * @author Itai Frenkel
 * @since 9.1.0
 */

public class CapacityRequirementsPerZones extends AbstractCapacityRequirementsPerKey {

    public CapacityRequirementsPerZones() {
    }
        
    public Set<ZonesConfig> getZones() {
        Set<ZonesConfig> zonesList = new HashSet<ZonesConfig>(); 
        for (String key : super.getKeys()) {
            zonesList.add(zonesFromString(key));
        }
        return zonesList;
    }
        
    public CapacityRequirementsPerZones set(ZonesConfig zones, CapacityRequirements capacity) {
        return (CapacityRequirementsPerZones) super.set(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones add(
            ZonesConfig zones, 
            CapacityRequirements capacity) {
        
      return (CapacityRequirementsPerZones) super.add(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(
            ZonesConfig zones, 
            CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtract(zonesToString(zones), capacity);
    }

    public CapacityRequirementsPerZones subtractZones(
            ZonesConfig zones) {
        return (CapacityRequirementsPerZones) super.subtractKey(zonesToString(zones));
    }

    public CapacityRequirementsPerZones subtractOrZero(
            ZonesConfig zones, CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtractOrZero(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.subtract(other);
    }

    public CapacityRequirementsPerZones add(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.add(other);
    }

    public CapacityRequirements getZonesCapacity(ZonesConfig zones) {
        return super.getKeyCapacity(zonesToString(zones));
    }

    
    
    public CapacityRequirements getZonesCapacityOrZero(ZonesConfig zones) {
        return super.getKeyCapacityOrZero(zonesToString(zones));
    }

    @Override
    protected CapacityRequirementsPerZones newZeroInstance() {
        return new CapacityRequirementsPerZones();
    }
}
