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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminAware;

/**
 * Gateways holds all the different {@link Gateway}s that are currently
 * discovered.
 * 
 * @author eitany
 * @since 8.0.4
 */
public interface Gateways extends AdminAware, Iterable<Gateway> {
    
    /**
     * Returns all the currently discovered gateways. 
     */
    Gateway[] getGateways();
    
    /**
     * Returns the {@link Gateway} for the given gateway name. Returns <code>null</code> if the gateway
     * is not currently discovered.
     */
    Gateway getGateway(String gatewayName);
    
    /**
     * Returns a map of {@link Gateway} keyed by their respective names.
     */
    Map<String, Gateway> getNames();
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till the gateway is identified as deployed. 
     * Return <code>null</code> if the gateway is not deployed
     * within the specified timeout.
     */
    Gateway waitFor(String gatewayName);

    /**
     * Waits for the specified timeout (in time interval) till the gateway is identified as deployed. Returns the
     * {@link Gateway}. Return <code>null</code> if the gateway is not deployed
     * within the specified timeout.
     */
    Gateway waitFor(String gatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns the number of gateways currently discovered.
     */
    int getSize();
    
    /**
     * Returns <code>true</code> if there are no gateways, <code>false</code> otherwise.
     */
    boolean isEmpty();
    
    //Events for addition and removal of gateways?
    
    //Direct access to gateway processing unit?
}
