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
package org.openspaces.interop;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainer;

import java.io.File;

/**
 * @author kimchy
 */
public class DotnetProcessingUnitContainerProvider implements DeployableProcessingUnitContainerProvider, ClusterInfoAware, BeanLevelPropertiesAware {


    private ClusterInfo clusterInfo;

    private BeanLevelProperties beanLevelProperties;

    public void setDeployPath(File deployPath) {
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
                
        DotnetProcessingUnitContainer dotnetpuContainer = new DotnetProcessingUnitContainer(clusterInfo, beanLevelProperties);
        
        return dotnetpuContainer;
    }

}
