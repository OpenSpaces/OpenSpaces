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
package org.openspaces.admin.pu.elastic.config;

import static org.openspaces.admin.internal.zone.config.ZonesConfigUtils.zonesFromString;
import static org.openspaces.admin.internal.zone.config.ZonesConfigUtils.zonesToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerZones;

/**
 * A key/value alternative to {@link CapacityRequirementsPerZone}
 * @author Itai Frenkel
 *
 */
public class CapacityRequirementsPerZonesConfig {

    private final StringProperties properties;
    private final String keyPrefix;
    
    public CapacityRequirementsPerZonesConfig() {
        this("",new CapacityRequirementsPerZones());
    }
    
    public CapacityRequirementsPerZonesConfig(CapacityRequirementsPerZones capacityRequirementsPerZones) {
        this("",capacityRequirementsPerZones);
    }
    
    public CapacityRequirementsPerZonesConfig(String keyPrefix, CapacityRequirementsPerZones capacityRequirementsPerZones) {
        this.keyPrefix = keyPrefix;
        this.properties = fromCapacityRequirementsPerZone(keyPrefix, capacityRequirementsPerZones);
    }
    
    public CapacityRequirementsPerZonesConfig(String keyPrefix, Map<String, String> properties) {
        this.properties = new StringProperties(properties);
        this.keyPrefix = keyPrefix;
    }

    public Map<String,String> getProperties() {
        return this.properties.getProperties();
    }

    public void addCapacity(ZonesConfig zones, ScaleStrategyCapacityRequirementConfig capacity) {

        CapacityRequirementsPerZones newCapacityPerZone = 
                toCapacityRequirementsPerZone()
                .add(zones, capacity.toCapacityRequirements());
        
        StringProperties capacityProperties = 
                fromCapacityRequirementsPerZone(keyPrefix,newCapacityPerZone);
        
        setCapacityProperties(capacityProperties);
    }

    
    private void setCapacityProperties(StringProperties capacityProperties) {
        for (final String key: new HashSet<String>(properties.getProperties().keySet())) {
            if (key.startsWith(keyPrefix)) {
                properties.remove(key);
            }
        }
        properties.putAll(capacityProperties.getProperties());
    }
    
    public CapacityRequirementsPerZones toCapacityRequirementsPerZone() {
        CapacityRequirementsPerZones capacityPerZone = new CapacityRequirementsPerZones();
        for (Entry<String, Map<String,String>> pair : groupPropertiesByZone(keyPrefix, properties).entrySet()) {
            CapacityRequirements capacity = new CapacityRequirementsConfig(pair.getValue()).toCapacityRequirements();
            ZonesConfig zones =  zonesFromString(pair.getKey());
            capacityPerZone = capacityPerZone.add(zones, capacity);
        }
        return capacityPerZone;
    }

    private static Map<String,Map<String, String>> groupPropertiesByZone(String keyPrefix, StringProperties properties) {
        HashMap<String, String> emptyMap = new HashMap<String,String>();
        StringProperties filteredProperties = properties;
        if (keyPrefix.length() > 0) {
            filteredProperties = new StringProperties(properties.getMap(keyPrefix, emptyMap));
        }
        Map<String,Map<String,String>> propertiesByZone = new HashMap<String, Map<String,String>>();
        for (String key : filteredProperties.getProperties().keySet()) {
            int zoneDelimiter = key.indexOf(".");
            String zonesList = key.substring(0,zoneDelimiter);
            if (!propertiesByZone.containsKey(zonesList)) {
                propertiesByZone.put(zonesList, filteredProperties.getMap(zonesList+".", emptyMap));
            }
        }
        return propertiesByZone;
    }

    private static StringProperties fromCapacityRequirementsPerZone(String keyPrefix, CapacityRequirementsPerZones capacityPerZone) {
        StringProperties capacityProperties = new StringProperties();
        for (ZonesConfig zones : capacityPerZone.getZones()) {
            final Map<String, String> zoneCapacityProperties = new CapacityRequirementsConfig(capacityPerZone.getZonesCapacity(zones)).getProperties();
            capacityProperties.putMap(keyPrefix + zonesToString(zones) + ".", zoneCapacityProperties);
        }
        return capacityProperties;
    }

    @Override
    public String toString() {
        return getPropertiesByZone().toString();
    }

    private Map<String, Map<String, String>> getPropertiesByZone() {
        return groupPropertiesByZone(keyPrefix, properties);
    }
   
}
