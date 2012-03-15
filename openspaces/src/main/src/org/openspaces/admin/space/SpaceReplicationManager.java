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
package org.openspaces.admin.space;

import org.openspaces.core.gateway.GatewayTarget;

/**
 * A replication manager for a single {@link Space}.
 * @author eitany
 * @since 9.0
 */
public interface SpaceReplicationManager {

    /**
     * Adds a new gateway target to the {@link Space} this manager manages, will wait until the gateway target is added in all
     * of the {@link SpaceInstance}s that belong to this space. 
     */
    void addGatewayTarget(GatewayTarget gatewayTarget); 
    
    /**
     * Removes an existing gateway target from the {@link Space} this manager manages, will wait until the gateway target is removed from all
     * of the {@link SpaceInstance}s that belong to this space. 
     */
    void removeGatewayTarget(String gatewayTargetName);
}
