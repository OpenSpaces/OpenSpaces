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
package org.openspaces.admin.internal.gsm;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.GridServiceManagers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

/**
 * @author kimchy
 */
public interface InternalGridServiceManagers extends GridServiceManagers {

    GridServiceManager[] getManagersNonFiltered();
    
    void addGridServiceManager(InternalGridServiceManager gridServiceManager);

    InternalGridServiceManager removeGridServiceManager(String uid);

    /**
     * Replaces the grid service manager, returning the old one
     */
    InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager);

    boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long remaining, TimeUnit milliseconds);

    ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology puDeployment, long timeout, TimeUnit timeUnit);
}
