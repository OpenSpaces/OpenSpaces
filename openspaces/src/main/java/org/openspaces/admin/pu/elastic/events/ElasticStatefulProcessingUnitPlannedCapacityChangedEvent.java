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
import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfig;
import org.openspaces.admin.pu.elastic.events.ElasticAutoScalingProgressChangedEvent;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitDecisionEvent;

import com.gigaspaces.internal.io.IOUtils;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class ElasticStatefulProcessingUnitPlannedCapacityChangedEvent 
    extends AbstractElasticProcessingUnitDecisionEvent
    implements ElasticAutoScalingProgressChangedEvent , ElasticProcessingUnitDecisionEvent{

    private static final long serialVersionUID = 1L;
    private CapacityRequirementsConfig beforePlanned;
    private CapacityRequirementsConfig afterPlanned;
    
    /**
    * de-serialization constructor
    */
    public ElasticStatefulProcessingUnitPlannedCapacityChangedEvent() {
        
    }
    
    public ElasticStatefulProcessingUnitPlannedCapacityChangedEvent(CapacityRequirementsConfig before, CapacityRequirementsConfig after) {
        this.beforePlanned = before;
        this.afterPlanned = after;
    }


    public CapacityRequirementsConfig getafterPlanned() {
        return afterPlanned;
    }

    public CapacityRequirementsConfig getbeforePlanned() {
        return beforePlanned;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        IOUtils.writeMapStringString(out, beforePlanned.getProperties());
        IOUtils.writeMapStringString(out, afterPlanned.getProperties());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        beforePlanned = new CapacityRequirementsConfig(IOUtils.readMapStringString(in));
        afterPlanned = new CapacityRequirementsConfig(IOUtils.readMapStringString(in));
    }

    @Override
    public String getDecisionDescription() {
        return "Planned capacity changed from " + beforePlanned + " to " + afterPlanned;
    }
}
