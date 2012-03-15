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
 * Represents a single delegation target
 * @author eitany
 * @since 8.0.4
 */
public interface GatewayDelegatorTarget {
    
    /**
     * Returns the delegator this delegation target is part of. 
     */
    GatewayDelegator getDelegator();
    
    /**
     * Returns the delegated target gateway name.
     */
    String getTargetGatewayName();
    
    /**
     * Returns <code>true</code> if the delegation is done via another delegator or <code>false</code> if this delegator is connected
     * directly to the target gateway. 
     */
    boolean isDelegateThroughOtherGateway();
    
    /**
     * Returns the name of the gateway delegator this delegator is connected to which routes communication to the target gateway. If this
     * delegator is connected directly to the target gateway it will return <code>null</code>.
     * @see #isDelegateThroughOtherGateway()
     */
    String getDelegateThroughGatewayName();

}
