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

import java.util.ArrayList;
import java.util.Collection;
import com.gigaspaces.internal.utils.StringUtils;
/**
 * 
 * @author Itai Frenkel
 * @since 9.1.0
 */

public class CapacityRequirementsPerZones extends AbstractCapacityRequirementsPerKey {

    public CapacityRequirementsPerZones() {
    }
        
    public Collection<String[]> getZones() {
        Collection<String[]> zonesList = new ArrayList<String[]>(); 
        for (String key : super.getKeys()) {
            zonesList.add(zonesFromString(key));
        }
        return zonesList;
    }
        
    public CapacityRequirementsPerZones set(String[] zones, CapacityRequirements capacity) {
        return (CapacityRequirementsPerZones) super.set(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones add(
            String[] zones, 
            CapacityRequirements capacity) {
        
      return (CapacityRequirementsPerZones) super.add(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(
            String[] zones, 
            CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtract(zonesToString(zones), capacity);
    }

    public CapacityRequirementsPerZones subtractZone(
            String[] zones) {
        return (CapacityRequirementsPerZones) super.subtractKey(zonesToString(zones));
    }

    public CapacityRequirementsPerZones subtractOrZero(
           String[] zones, CapacityRequirements capacity) {
        
        return (CapacityRequirementsPerZones) super.subtractOrZero(zonesToString(zones), capacity);
    }
    
    public CapacityRequirementsPerZones subtract(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.subtract(other);
    }

    public CapacityRequirementsPerZones add(CapacityRequirementsPerZones other) {
        return (CapacityRequirementsPerZones) super.add(other);
    }

    public CapacityRequirements getZonesCapacity(String[] zones) {
        return super.getKeyCapacity(zonesToString(zones));
    }

    private String zonesToString(String[] zones) {
        return StringUtils.arrayToCommaDelimitedString(zones);
    }

    private String[] zonesFromString(String key) {
        return StringUtils.commaDelimitedListToStringArray(key);
    }
    
    public CapacityRequirements getZonesCapacityOrZero(String[] zones) {
        return super.getKeyCapacityOrZero(zonesToString(zones));
    }

    @Override
    protected CapacityRequirementsPerZones newZeroInstance() {
        return new CapacityRequirementsPerZones();
    }
}
