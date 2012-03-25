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
import org.openspaces.grid.gsm.strategy.EagerScaleStrategyBean;

/*
 * Defines an automatic scaling strategy that consumes capacity eagerly.
 * When enabled the processing unit scales out whenever free capacity that meets zone and isolation constraints is available.
 * 
 * @see EagerScaleStrategyConfigurer
 * @author itaif
 */
public class EagerScaleConfig 
        implements ScaleStrategyConfig , Externalizable {

    private static final long serialVersionUID = 1L;
 
    private StringProperties properties;
    public EagerScaleConfig(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public EagerScaleConfig() {
        this(new HashMap<String,String>());
    }
    
    @Override
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }
    
    @Override
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties, pollingIntervalSeconds);
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
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    @Override
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public String getBeanClassName() {
        return EagerScaleStrategyBean.class.getName();
    }
    
    @Override
    public String toString() {
        return this.properties.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        return (other instanceof EagerScaleConfig) &&
                this.properties.equals(((EagerScaleConfig)other).properties);
    }
    
    @Override
    public int hashCode() {
        return this.properties.hashCode();
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



