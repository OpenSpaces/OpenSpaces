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
package org.openspaces.grid.gsm.machines.plugins.events;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

/**
 * @author Itai Frenkel
 * @since 9.1.0
 */
public class GridServiceAgentStartRequestedEvent extends AbstractGridServiceAgentProvisioningEvent {

    private static final long serialVersionUID = 1L;
    private String hostAddress;
    
    /**
     * Deserialization cotr
     */
    public GridServiceAgentStartRequestedEvent() {
    }
    
    public GridServiceAgentStartRequestedEvent(String hostAddress) {
        super();
        this.hostAddress = hostAddress;
    }
    
    
    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeString(out, hostAddress);    
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        hostAddress = IOUtils.readString(in);    
    }

    @Override
    public String getDecisionDescription() {
         return "Installing agent on machine " + hostAddress;
    }
}