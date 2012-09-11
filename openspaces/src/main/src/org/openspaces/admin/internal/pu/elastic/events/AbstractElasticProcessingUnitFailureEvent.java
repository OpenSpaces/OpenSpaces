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
package org.openspaces.admin.internal.pu.elastic.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

public abstract class AbstractElasticProcessingUnitFailureEvent implements InternalElasticProcessingUnitFailureEvent {
    private static final long serialVersionUID = -4093118769084514194L;
    
    private String failureDescription;
    private String[] processingUnitNames;
    
    /**
     * de-serialization constructor
     */
    public AbstractElasticProcessingUnitFailureEvent() {
    }
    
    @Override
    public String[] getProcessingUnitNames() {
        return processingUnitNames;
    }

    @Override
    public String getFailureDescription() {
        return failureDescription;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeString(out, failureDescription);
        IOUtils.writeStringArray(out, processingUnitNames);
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        failureDescription = IOUtils.readString(in);
        processingUnitNames = IOUtils.readStringArray(in);
    }

    public void setProcessingUnitNames(String[] processingUnitNames) {
        this.processingUnitNames = processingUnitNames;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription; 
    }
    
    @Override
    public String toString() {
        return getFailureDescription();
    }
}
