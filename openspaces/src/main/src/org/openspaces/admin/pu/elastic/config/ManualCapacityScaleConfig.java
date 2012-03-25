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
package org.openspaces.admin.pu.elastic.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.ManualCapacityScaleStrategyBean;

/**
 * Defines a manual scaling strategy that consumes the specified memory capacity.
 * When enabled the processing unit scales out whenever the specified memory capacity deviates from the actual memory capacity.
 * 
 * When a backup partition is enabled (which usually is the case), the specified memory capacity is the total memory occupied by the
 * primary partition instances and the backup partition instances.
 *  
 * @see ManualCapacityScaleConfigurer
 * @since 8.0
 * @author itaif
 */
public class ManualCapacityScaleConfig 
    implements ScaleStrategyConfig , ScaleStrategyCapacityRequirementConfig, Externalizable {

    private static final long serialVersionUID = 1L;

    private CapacityRequirementConfig capacityRequirementConfig;
    
    private StringProperties properties;
    
    /**
     * Default constructor
     */
    public ManualCapacityScaleConfig() {
        this(new HashMap<String,String>());
    }
    
    public ManualCapacityScaleConfig(Map<String,String> properties) {
        setProperties(properties);
    }

    @Override
    public void setMemoryCapacityInMB(long memory) {
        capacityRequirementConfig.setMemoryCapacityInMB(memory);
    }

    @Override
    public long getMemoryCapacityInMB() throws NumberFormatException {
        return capacityRequirementConfig.getMemoryCapacityInMB();
    }
    
    @Override
    public void setNumberOfCpuCores(double cpuCores) {
        capacityRequirementConfig.setNumberOfCpuCores(cpuCores);
    }

    @Override
    public double getNumberOfCpuCores() {
        return capacityRequirementConfig.getNumberOfCpuCores();
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

    /*
     * @see ManualCapacityScaleConfig#isAtMostOneContainerPerMachine()
     */
    @Deprecated
    public boolean isAtMostOneContainersPerMachine() {
        return isAtMostOneContainerPerMachine();
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
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
        this.capacityRequirementConfig = new CapacityRequirementConfig(properties);
    }

    @Override
    public String getBeanClassName() {
        return ManualCapacityScaleStrategyBean.class.getName();
    }
   
    @Override
    public String toString() {
        return this.properties.toString();
    }

    @Override
    public void setDrivesCapacityInMB(Map<String,Long> megaBytesPerDrive) {
        capacityRequirementConfig.setDrivesCapacityInMB(megaBytesPerDrive);
    }
    
    @Override
    public Map<String,Long> getDrivesCapacityInMB() throws NumberFormatException{
        return capacityRequirementConfig.getDrivesCapacityInMB();
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
        ManualCapacityScaleConfig other = (ManualCapacityScaleConfig) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
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
}
