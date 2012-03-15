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
package org.openspaces.grid.esm;

import java.rmi.RemoteException;
import java.util.Map;

import org.jini.rio.monitor.event.Events;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;

    void setProcessingUnitScaleStrategy(String puName, ScaleStrategyConfig scaleStrategyConfig) throws RemoteException;

    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(String processingUnitName) throws RemoteException;
    
    Events getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents) throws RemoteException;
}
