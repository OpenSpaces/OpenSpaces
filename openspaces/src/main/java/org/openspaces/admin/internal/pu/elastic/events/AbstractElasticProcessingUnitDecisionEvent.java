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
package org.openspaces.admin.internal.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class AbstractElasticProcessingUnitDecisionEvent extends AbstractElasticProcessingUnitProgressChangedEvent implements InternalElasticProcessingUnitDecisionEvent {

    private static final long serialVersionUID = 1L;
    
    private String decisionDescription;
    
    @Override
    public void setDecisionDescription(String description) {
        this.decisionDescription = description;
    }
    
    @Override
    public String getDecisionDescription() {
        return decisionDescription;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeString(out, decisionDescription);    
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        decisionDescription = IOUtils.readString(in);    
    }
    
    @Override
    public String toString() {
        return getDecisionDescription();
    }

    
}
