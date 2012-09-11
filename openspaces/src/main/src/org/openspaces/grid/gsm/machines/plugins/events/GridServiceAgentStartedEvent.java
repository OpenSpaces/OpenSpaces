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
public class GridServiceAgentStartedEvent extends AbstractGridServiceAgentProvisioningEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String hostAddress;
    private String agentUid;

    /**
     * Deserialization cotr
     */
    public GridServiceAgentStartedEvent() {
    }
    
    public GridServiceAgentStartedEvent(String hostAddress, String agentUid) {
        setDecisionDescription("New agent was started. Host address " + hostAddress + " Agent UID " + agentUid);
        this.hostAddress = hostAddress;
        this.agentUid = agentUid;
    }
    
    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(String agentUid) {
        this.agentUid = agentUid;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeString(out, hostAddress);
        IOUtils.writeString(out, agentUid);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        hostAddress = IOUtils.readString(in);
        agentUid = IOUtils.readString(in);
    }
}
