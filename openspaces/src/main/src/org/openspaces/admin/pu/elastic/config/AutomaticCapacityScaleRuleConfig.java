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
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.AutomaticCapacityScaleStrategyBean;

/**
 * Configures an automatic scaling rule based on a monitred statistics and thresholds.
 * @author itaif
 * @since 9.0.0
 * @see AutomaticCapacityScaleConfig
 * @see AutomaticCapacityScaleRuleConfigurer
 */
public class AutomaticCapacityScaleRuleConfig 
    implements ScaleStrategyConfig , Externalizable {

    private static final long serialVersionUID = 1L;

    private static final Map<String,String> STATISTICS_DEFAULT = new ProcessingUnitStatisticsId().getProperties();

    private static final String STATISTICS_KEY_PREFIX = "statistics.";
    private static final String LOW_THRESHOLD_KEY = "low-threshold";
    private static final Double LOW_THRESHOLD_DEFAULT = Double.MIN_VALUE;
    private static final String HIGH_THRESHOLD_KEY = "high-threshold";
    private static final Double HIGH_THRESHOLD_DEFAULT = Double.MAX_VALUE;

    
    private StringProperties properties;

    public AutomaticCapacityScaleRuleConfig() {
        this.properties = new StringProperties();
    }
    
    public AutomaticCapacityScaleRuleConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }

    /**
     * Defines the statistics that is compared against the high and low thresholds
     */
    public void setStatistics(ProcessingUnitStatisticsId statisticsId) {
        properties.putMap(STATISTICS_KEY_PREFIX, statisticsId.getProperties());
    }
    
    public ProcessingUnitStatisticsId getStatistics() {
        return new ProcessingUnitStatisticsId(
                properties.getMap(STATISTICS_KEY_PREFIX, STATISTICS_DEFAULT));
    }

    /**
     * Defines a low threshold that triggers an increase or decrease of capacity
     * @see setLowThresholdCapacityDecrease
     * @see setLowThresholdCapacityIncrease
     */
    public void setLowThreshold(double lowThreshold) {
        properties.putDouble(LOW_THRESHOLD_KEY, lowThreshold);        
    }
    
    public double getLowThreshold() {
        return properties.getDouble(LOW_THRESHOLD_KEY, LOW_THRESHOLD_DEFAULT);
    }
    
    /**
     * Defines a high threshold that triggers an increase or decrease of capacity
     * @see setLowThresholdCapacityDecrease
     * @see setLowThresholdCapacityIncrease
     */
    public void setHighThreshold(double highThreshold) {
        properties.putDouble(HIGH_THRESHOLD_KEY, highThreshold);        
    }
    
    public double getHighThreshold() {
        return properties.getDouble(HIGH_THRESHOLD_KEY, HIGH_THRESHOLD_DEFAULT);
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
    public String getBeanClassName() {
        return AutomaticCapacityScaleStrategyBean.class.getName();
    }
    
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public Map<String, String> getProperties() {
        return properties.getProperties();
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
        AutomaticCapacityScaleRuleConfig other = (AutomaticCapacityScaleRuleConfig) obj;
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
