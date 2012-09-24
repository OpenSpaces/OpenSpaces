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

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitDecisionEvent;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent 
    extends AbstractElasticProcessingUnitDecisionEvent
    implements ElasticAutoScalingProgressChangedEvent {

    private static final long serialVersionUID = 1L;
    private int beforePlannedNumberOfInstances;
    private int afterPlannedNumberOfInstances;
    
    /**
    * de-serialization constructor
    */
    public ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent() {
        
    }
    
    public ElasticStatelessProcessingUnitPlannedNumberOfInstancesChangedEvent(int beforePlannedNumberOfInstances, int afterPlannedNumberOfInstances) {
        this.beforePlannedNumberOfInstances = beforePlannedNumberOfInstances;
        this.afterPlannedNumberOfInstances = afterPlannedNumberOfInstances;
    }


    public int getAfterPlannedNumberOfInstances() {
        return afterPlannedNumberOfInstances;
    }

    public int getBeforePlannedNumberOfInstances() {
        return beforePlannedNumberOfInstances;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(beforePlannedNumberOfInstances);
        out.writeInt(afterPlannedNumberOfInstances);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        beforePlannedNumberOfInstances = in.readInt();
        afterPlannedNumberOfInstances = in.readInt();
    }

    @Override
    public String getDecisionDescription() {
        return "Number of planned instances changed from " + beforePlannedNumberOfInstances + " to " + afterPlannedNumberOfInstances;
    }
}
