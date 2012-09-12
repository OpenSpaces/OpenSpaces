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

/**
 * A gateway is a composition of one or more of {@link GatewayProcessingUnit}s, and it is in charge of 
 * replication between different {@link org.openspaces.admin.space.Space}s. e.g. Replication between two sites over WAN.
 * 
 * @author eitany
 * @since 8.0.4
 */
public interface Gateway extends Iterable<GatewayProcessingUnit>{
    
    /**
     * Returns all the currently deployed {@link GatewayProcessingUnit}s. 
     */
    GatewayProcessingUnit[] getGatewayProcessingUnits();
    
    /**
     * Returns the name which is used by the other gateways to locate this gateway.
     */
    String getName();
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * at least the provided number of Gateway Processing Unit Instances are up. Returns <code>true</code> if
     * the specified number of gateway processing units are deployed, <code> false</code> otherwise;
     */
    boolean waitFor(int numberOfGatewayProcessingUnits);
    
    /**
     * Waits for the specified timeout (in time interval) till at least the provided number of Gateway Processing Unit Instances are up. 
     * Returns <code>true</code> if the specified number of gateway processing units are deployed within the specified timeout, 
     * <code>false</code> otherwise;
     */
    boolean waitFor(int numberOfGatewayProcessingUnits, long timeout, TimeUnit timeUnit);
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * the processing unit of this gateway is identified as deployed. Return <code>null</code> if the processing unit is not deployed
     * within the specified timeout.
     */
    GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName);

    /**
     * Waits for the specified timeout (in time interval) till 
     * the processing unit of this gateway is identified as deployed. Return <code>null</code> if the processing unit is not deployed
     * within the specified timeout.
     */
    GatewayProcessingUnit waitForGatewayProcessingUnit(String processingUnitName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns the gateway processing unit for the given processing unit name. Returns <code>null</code> if the gateway 
     * processing unit is not currently discovered.
     */
    GatewayProcessingUnit getGatewayProcessingUnit(String processingUnitName);
    
    /**
     * Returns a map of {@link GatewayProcessingUnit} keyed by their respective names.
     */
    Map<String, GatewayProcessingUnit> getNames();
    
    /**
     * Returns a gateway sink which has a source gateway with the given name. Returns <code>null</code> if the gateway sink is 
     * not currently discovered or this gateway has no sink with source gateway with the given name.
     */
    GatewaySink getSink(String sourceGatewayName);
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * the gateway sink with the given source gateway name is identified as deployed. Return <code>null</code> 
     * if the sink is not deployed within the specified timeout.
     */
    GatewaySink waitForSink(String sourceGatewayName);
    
    /**
     * Waits for the specified timeout (in time interval) till 
     * the gateway sink with the given source gateway name is identified as deployed. Return <code>null</code> 
     * if the sink is not deployed within the specified timeout.
     */
    GatewaySink waitForSink(String sourceGatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns a sink source gateway with the given name. Returns <code>null</code> if the sink source is 
     * not currently discovered or this gateway has no sink with source gateway with the given name.
     */
    GatewaySinkSource getSinkSource(String sourceGatewayName);
    
    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * the gateway sink with the given source gateway name is identified as deployed. Returns <code>null</code> 
     * if the sink source is not deployed within the specified timeout.
     */
    GatewaySinkSource waitForSinkSource(String sourceGatewayName);
    
    /**
     * Waits for the specified timeout (in time interval) till 
     * the gateway sink with the given source gateway name is identified as deployed. Returns <code>null</code> 
     * if the sink source is not deployed within the specified timeout.
     */
    GatewaySinkSource waitForSinkSource(String sourceGatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns a gateway delegator which has a target gateway with the given name. Returns <code>null</code> if the gateway delegator is 
     * not currently discovered or this gateway has no target with target gateway with the given name.
     */
    GatewayDelegator getDelegator(String targetGatewayName);

    /**
     * Waits for the default timeout specified by {@link Admin#setDefaultTimeout(long, TimeUnit)} till 
     * the gateway delegator with the given target gateway name is identified as deployed. Returns <code>null</code> 
     * if the delegator target is not deployed within the specified timeout.
     */
    GatewayDelegator waitForDelegator(String targetGatewayName);
    
    /**
     * Waits for the specified timeout (in time interval) till 
     * the gateway delegator with the given target gateway name is identified as deployed. Returns <code>null</code> 
     * if the delegator target is not deployed within the specified timeout.
     */
    GatewayDelegator waitForDelegator(String targetGatewayName, long timeout, TimeUnit timeUnit);
    
    /**
     * Returns the number of deployed {@link GatewayProcessingUnit} which are part of this gateway.  
     */
    int getSize();
    
    /**
     * Returns <code>true</code> if there are no deployed {@link GatewayProcessingUnit} which are part of this gateway; <code>false<code> otherwise.
     */
    boolean isEmpty();
    
    //Events for addition and removal of gateways processing units
}
