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
package org.openspaces.admin.pu.dependency;

import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.pu.container.support.RequiredDependenciesCommandLineParser;

/**
 * Defines a dependency that postpones the processing unit instance deployment.
 * The instance is deployed only after the dependencies described by this interface are met.
 * 
 * @since 8.0.6
 * @author itaif
 *
 */
public interface ProcessingUnitDeploymentDependencies<T extends ProcessingUnitDependency> 
    extends ProcessingUnitDetailedDependencies<T> {

    public final String COMMANDLINE_OPTION = RequiredDependenciesCommandLineParser.INSTANCE_DEPLOYMENT_REQUIRED_DEPENDENCIES_PARAMETER_NAME;

}
