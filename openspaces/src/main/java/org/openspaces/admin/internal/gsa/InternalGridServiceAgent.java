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
package org.openspaces.admin.internal.gsa;

import net.jini.core.lookup.ServiceID;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.internal.support.InternalGridComponent;

import com.gigaspaces.grid.gsa.AgentProcessesDetails;
import com.gigaspaces.grid.gsa.GSA;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgent extends GridServiceAgent, InternalGridComponent {

    ServiceID getServiceID();

    GSA getGSA();

    void setProcessesDetails(AgentProcessesDetails processesDetails);

    void kill(InternalAgentGridComponent agentGridComponent);

    void restart(InternalAgentGridComponent agentGridComponent);

    int internalStartGridService(GridServiceContainerConfig config);

    /**
     * Invoked to mark a child grid component as added to lookup service
     * @since 9.0.1
     * @author itaif
     */
    void addAgentGridComponent(InternalAgentGridComponent agentGridComponent);
    
    /**
     * Invoked to mark a child grid component as removed from lookup service
     * @since 9.0.1
     * @author itaif
     */
    void removeAgentGridComponent(InternalAgentGridComponent agentGridComponent);
    
    /**
     * @return all child grid components marked as removed from lookup service, but are still managed by this agent
     * This is a temporary condition that could happen if the grid component is experiencing GC 
     * or has other delays updating the lookup lease.
     * @since 9.0.1
     * @author itaif 
     */
    InternalAgentGridComponent[] getUnconfirmedRemovedAgentGridComponents();
    
}
