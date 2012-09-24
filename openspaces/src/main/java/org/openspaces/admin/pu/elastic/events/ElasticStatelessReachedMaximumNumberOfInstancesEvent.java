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

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ElasticStatelessReachedMaximumNumberOfInstancesEvent 
    extends AbstractElasticProcessingUnitFailureEvent
    implements ElasticAutoScalingFailureEvent {

    private static final long serialVersionUID = 1L;
    private int existingNumberOfInstances;
    private int requestedNumberOfInstances; 
    private int maximumNumberOfInstances;
    
    /**
    * de-serialization constructor
    */
    public ElasticStatelessReachedMaximumNumberOfInstancesEvent() {
        
    }
    
    public ElasticStatelessReachedMaximumNumberOfInstancesEvent(ProcessingUnit pu, int existingNumberOfInstances, int requestedNumberOfInstances, int maximumNumberOfInstances) {
        this.existingNumberOfInstances = existingNumberOfInstances;
        this.requestedNumberOfInstances = requestedNumberOfInstances;
        this.maximumNumberOfInstances = maximumNumberOfInstances;
        setProcessingUnitName(pu.getName());
        //TODO: Add statistics and threshold values 
        setFailureDescription(pu.getName() + " cannot increase from " + existingNumberOfInstances + " instances to " + requestedNumberOfInstances
                + " instances, since it breaches maximum of " + maximumNumberOfInstances + " instances");
    }

    public int getExistingNumberOfInstances() {
        return existingNumberOfInstances;
    }
    
    public int getRequestedNumberOfInstances() {
        return requestedNumberOfInstances;
    }
    
    public int getMaximumNumberOfInstances() {
        return maximumNumberOfInstances;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(existingNumberOfInstances);
        out.writeInt(requestedNumberOfInstances);
        out.writeInt(maximumNumberOfInstances);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        existingNumberOfInstances = in.readInt();
        requestedNumberOfInstances = in.readInt();
        maximumNumberOfInstances = in.readInt();
    }
}
