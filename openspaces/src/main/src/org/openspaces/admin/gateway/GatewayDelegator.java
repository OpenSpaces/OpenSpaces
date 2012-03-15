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
package org.openspaces.admin.gateway;

/**
 * A gateway delegator is used to delegate outgoing replication packets to other gateways. Acts as a communication connector such that a local space 
 * and relevant components need only to find the delegator locally in order to communicate with a remote gateway.
 * Delegators can be chained together to create a multi-hop delegation between gateways. 
 * @author eitany
 * @since 8.0.4
 * @see Gateway
 */
public interface GatewayDelegator {
    
    /**
     * Returns the gateway this sink is part of.
     */
    GatewayProcessingUnit getGatewayProcessingUnit();
    
    /**
     * Returns all the delegation targets of this delegator. 
     */
    GatewayDelegatorTarget[] getDelegationTargets();

    /**
     * Returns <code>true</code> if this delegator has a target gateway with the specified name; <code>false</code> otherwise.
     */
    boolean containsTarget(String targetGatewayName); 

}
