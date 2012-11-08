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
package org.openspaces.admin.internal.support;

import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.InternalGridServiceAgent;

import com.gigaspaces.internal.jvm.JVMDetails;

/**
 * @author kimchy
 */
public abstract class AbstractAgentGridComponent extends AbstractGridComponent implements InternalAgentGridComponent {

    private final int agentId;

    private final String agentUid;

    private volatile GridServiceAgent gridServiceAgent;

    public AbstractAgentGridComponent(InternalAdmin admin, int agentId, String agentUid, JVMDetails jvmDetails) {
        super(admin, jvmDetails);
        this.agentId = agentId;
        this.agentUid = agentUid;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public String getAgentUid() {
        return this.agentUid;
    }

    public GridServiceAgent getGridServiceAgent() {
        return gridServiceAgent;
    }

    public void setGridServiceAgent(GridServiceAgent gridServiceAgent) {
        assertStateChangesPermitted();
        this.gridServiceAgent = gridServiceAgent;
    }

    public void kill() {
        if (gridServiceAgent == null) {
            throw new AdminException("Not associated with an agent to perform kill operation");
        }
        ((InternalGridServiceAgent) gridServiceAgent).kill(this);
    }

    public void restart() {
        if (gridServiceAgent == null) {
            throw new AdminException("Not associated with an agent to perform restart operation");
        }
        ((InternalGridServiceAgent) gridServiceAgent).restart(this);
    }
}
