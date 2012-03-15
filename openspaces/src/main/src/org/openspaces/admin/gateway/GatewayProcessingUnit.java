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

import org.openspaces.admin.GridComponent;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * A Gateway Processing unit is the building block of a {@link Gateway}. 
 * It has one to one relationship with deployed {@link ProcessingUnit} which hosts this gateway component.
 * @author eitany
 * @since 8.0.4
 */
public interface GatewayProcessingUnit extends GridComponent{
    
    /**
     * Returns the {@link Gateway} this gateway processing unit belongs to.
     */
    Gateway getGateway();
    
    /**
     * Returns the hosting {@link ProcessingUnit}.
     */
    ProcessingUnit getProcessingUnit();

    /**
     * Returns the sink of this gateway or <code>null</code> if no sink exists in this gateway. 
     */
    GatewaySink getSink();
    
    /**
     * Returns the delegator of this gateway or <code>null</code> if no delegator exists in this gateway. 
     */
    GatewayDelegator getDelegator();
}
