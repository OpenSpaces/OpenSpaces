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
package org.openspaces.admin.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitDecisionEvent;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;

import com.gigaspaces.internal.io.IOUtils;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent 
    extends AbstractElasticProcessingUnitDecisionEvent
    implements ElasticAutoScalingProgressChangedEvent {

    private static final long serialVersionUID = 1L;
    private int oldPlannedNumberOfInstances;
    private int newPlannedNumberOfInstances;
    private int actualNumberOfInstances;
    private AutomaticCapacityScaleRuleConfig rule;
    private boolean highThresholdBreached;
    private String metricValue;
    
    /**
    * de-serialization constructor
    */
    public ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent() {
        
    }
    
    /**
     * @param actualNumberOfInstances - number of instances currently deployed
     * @param oldPlannedNumberOfInstances - planned number of instances before rule breached
     * @param newPlannedNumberOfInstances - planned number of instances after rule breached
     */
    public ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent(
            int actualNumberOfInstances, 
            int oldPlannedNumberOfInstances,
            int newPlannedNumberOfInstances) {
       
        this (actualNumberOfInstances, oldPlannedNumberOfInstances, newPlannedNumberOfInstances, null, false, null); 
    }
            
    /**
     * @param actualNumberOfInstances - number of instances currently deployed
     * @param oldPlannedNumberOfInstances - planned number of instances before rule breached
     * @param newPlannedNumberOfInstances - planned number of instances after rule breached
     * @param rule - the rule that was breached
     * @param highThresholdBreached - true means high threshold breached, false means low threshold breached
     * @param metricValue - the metric value that breached the threshold as a string.
     */
    public ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent(
            int actualNumberOfInstances, 
            int oldPlannedNumberOfInstances,
            int newPlannedNumberOfInstances, 
            AutomaticCapacityScaleRuleConfig rule, 
            boolean highThresholdBreached, 
            String metricValue) {
        this.actualNumberOfInstances = actualNumberOfInstances;
        this.oldPlannedNumberOfInstances = oldPlannedNumberOfInstances;
        this.newPlannedNumberOfInstances = newPlannedNumberOfInstances;
        this.rule = rule;
        this.highThresholdBreached = highThresholdBreached;
        this.metricValue = metricValue;
    }


    public int getNewPlannedNumberOfInstances() {
        return newPlannedNumberOfInstances;
    }

    public int getBeforePlannedNumberOfInstances() {
        return oldPlannedNumberOfInstances;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(oldPlannedNumberOfInstances);
        out.writeInt(newPlannedNumberOfInstances);
        out.writeInt(actualNumberOfInstances);
        out.writeBoolean(highThresholdBreached);
        IOUtils.writeString(out, metricValue);
        writeRule(out, rule);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        oldPlannedNumberOfInstances = in.readInt();
        newPlannedNumberOfInstances = in.readInt();
        actualNumberOfInstances = in.readInt();
        highThresholdBreached = in.readBoolean();
        metricValue = IOUtils.readString(in);
        rule = readRule(in);
    }
    
    private void writeRule(ObjectOutput out, AutomaticCapacityScaleRuleConfig rule) throws IOException {
        
        final Map<String, String> properties = null;
        if (rule != null) {
            rule.getProperties();
        }
        
        IOUtils.writeMapStringString(out, properties);
    }

    private static AutomaticCapacityScaleRuleConfig readRule(ObjectInput in) throws IOException, ClassNotFoundException {
        AutomaticCapacityScaleRuleConfig rule = null;
        Map<String,String> properties = IOUtils.readMapStringString(in);
        if (properties != null) {
            rule = new AutomaticCapacityScaleRuleConfig(properties);
        }
        return rule;
    }

    @Override
    public String getDecisionDescription() {
        StringBuilder desc = new StringBuilder();
        
        if (newPlannedNumberOfInstances > actualNumberOfInstances) {
            desc.append("Scaling out. ");
        }
        else if (newPlannedNumberOfInstances < actualNumberOfInstances) {
            desc.append("Scaling in. ");
        }
        
        if (rule != null) {
            desc.append(rule.getStatistics().getMetric())
            .append(" (")
            .append(metricValue)
            .append(") is ");
            
            if (highThresholdBreached) {
                desc.append("above high threshold (" + rule.getHighThreshold() +"). ");
            }
            else {
                desc.append("below low threshold (" + rule.getLowThreshold() +"). ");
            }
        }
        if (oldPlannedNumberOfInstances != newPlannedNumberOfInstances) {
            desc
            .append("Number of planned instances changed from ")
            .append(oldPlannedNumberOfInstances)
            .append(" to ")
            .append(newPlannedNumberOfInstances)
            .append(". ");
        }
        else {
            desc
            .append("Number of planned instances is ")
            .append(newPlannedNumberOfInstances)
            .append(". ");
        }
        
        desc
        .append("Actual number of instances is ")
        .append(actualNumberOfInstances)
        .append(". ");
        
        return desc.toString();
    }

    public AutomaticCapacityScaleRuleConfig getRule() {
        return rule;
    }

    public boolean isHighThresholdBreached() {
        return rule != null && highThresholdBreached;
    }

    public boolean isLowThresholdBreached() {
        return rule != null && !highThresholdBreached;
    }
    
    public String getMetricValue() {
        return metricValue;
    }
}
