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
package org.openspaces.admin.internal.application;

import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

public class DefaultApplicationDeploymentOptions implements InternalApplicationDeploymentOptions {

    private final String applicationName;
    private final List<ProcessingUnitDeploymentTopology> processingUnitDeployments;

    public DefaultApplicationDeploymentOptions(
            String applicationName,
            List<ProcessingUnitDeploymentTopology> processingUnitDeployments) {

        this.applicationName = applicationName;
        this.processingUnitDeployments = processingUnitDeployments;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public ProcessingUnitDeployment[] getProcessingUnitDeployments(Admin admin) {
        ProcessingUnitDeployment[] puDeployments = new ProcessingUnitDeployment[processingUnitDeployments.size()];
        for (int i = 0 ; i < puDeployments.length ; i++) {
            puDeployments[i] = processingUnitDeployments.get(i).toProcessingUnitDeployment(admin);
        }
        return puDeployments;
    }

    
}
