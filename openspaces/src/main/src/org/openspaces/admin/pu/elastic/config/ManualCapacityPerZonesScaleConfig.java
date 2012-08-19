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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.ManualCapacityPerZonesScaleStrategyBean;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 * 
 */
public class ManualCapacityPerZonesScaleConfig implements ScaleStrategyConfig , ScaleStrategyAgentZonesAffinityConfig, Externalizable {

    private static final long serialVersionUID = 1L;

    private static final String ZONE_PREFIX = "capacity-per-zone.";
    
    private StringProperties properties;
    private CapacityRequirementsPerZonesConfig capacityPerZone;
    
    public ManualCapacityPerZonesScaleConfig() {
        this(new HashMap<String,String>());
    }
    
    public ManualCapacityPerZonesScaleConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
        capacityPerZone = new CapacityRequirementsPerZonesConfig(ZONE_PREFIX, properties);
    }

    /**
     * @param zone - the location (expressed as a GridServiceAgent zone) in which the capacity is allocated 
     * @param capacityRequirements - the capacity to allocate
     */
    public void addCapacity(ExactZonesConfig zones, CapacityRequirementsConfig capacity) {
        capacityPerZone.addCapacity(zones, capacity);
    }


    @Override
    public void setPollingIntervalSeconds(int seconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties,seconds);
    }
    
    @Override
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }

    @Override
    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    @Override
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
    
    @Override
    public boolean isAtMostOneContainerPerMachine() {
        return ScaleStrategyConfigUtils.isSingleContainerPerMachine(properties);
    }

    @Override
    public void setAtMostOneContainerPerMachine(boolean atMostOneContainerPerMachine) {
        ScaleStrategyConfigUtils.setAtMostOneContainerPerMachine(properties, atMostOneContainerPerMachine);
    }
    
    @Override
    public boolean isGridServiceAgentZonesAffinity() {
        return ScaleStrategyConfigUtils.isGridServiceAgentZonesAffinity(properties);
    }
    
    @Override
    public void setGridServiceAgentZonesAffinity(boolean enableAgentZonesAffinity) {
        ScaleStrategyConfigUtils.setGridServiceAgentZonesAffinity(properties, enableAgentZonesAffinity);
    }
    
    public CapacityRequirementsPerZonesConfig getCapacityRequirementsPerZonesConfig() {
        return capacityPerZone;
    }
    
    @Override
    public String getBeanClassName() {
        return ManualCapacityPerZonesScaleStrategyBean.class.getName();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
        capacityPerZone = new CapacityRequirementsPerZonesConfig(ZONE_PREFIX, properties);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.properties.getProperties());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.properties = new StringProperties((Map<String,String>)in.readObject());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManualCapacityPerZonesScaleConfig other = (ManualCapacityPerZonesScaleConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }
}
