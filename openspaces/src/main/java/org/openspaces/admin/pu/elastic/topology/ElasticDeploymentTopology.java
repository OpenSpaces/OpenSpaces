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
package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

public interface ElasticDeploymentTopology extends ProcessingUnitDeploymentTopology {

    /**
     * Sets the processing unit name that will be deployed. By default it will be based on the
     * parameter passed in the constructor.
     */
    ElasticDeploymentTopology name(String name);
    
    /**
     * Defines a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    ElasticDeploymentTopology addContextProperty(String key, String value);

    /**
     * Will cause the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started using a script
     * and not a pure Java process.
     */
    ElasticDeploymentTopology useScriptToStartContainer();

    /**
     * Will cause JVM options added using {@link #commandLineArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    ElasticDeploymentTopology overrideCommandLineArguments();

    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     * 
     * This method does not conform to the fluent API naming conventions. Use addCommandLineArgument instead.
     */
    ElasticDeploymentTopology commandLineArgument(String vmInputArgument);
    
    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     *  
     */
    ElasticDeploymentTopology addCommandLineArgument(String vmInputArgument);

    /**
     * Defines an environment variable that will be passed to forked process.
     * 
     * This method does not conform to the fluent API naming conventions. Use addEnvironmnetVariable instead.
     */
    ElasticDeploymentTopology environmentVariable(String name, String value);
    
    /**
     * Defines an environment variable that will be passed to forked process.
     * 
     */
    ElasticDeploymentTopology addEnvironmentVariable(String name, String value);

    /**
     * Configure the server side bean that starts and stops machines automatically. For example, the
     * bean could delegate the request to a cloud provider.
     * <p>
     * The machines returned by the 'machine provisioner' will be <b>dedicated</b> to the instances
     * of this processing unit. In other words, this processing unit will <u>not share</u> the
     * machines with other processing units.
     * <p>
     * See also {@link DiscoveredMachineProvisioningConfig} for configuring deployment on a non-virtualized environment. Machines are discovered
     * if 'Grid Service Agents' are running on them.
     * @see #sharedMachineProvisioning(String, ElasticMachineProvisioningConfig)
     */
    ElasticDeploymentTopology dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config);

    /**
     * Configure the server side bean that starts and stops machines automatically. For example, the
     * bean could delegate the request to a cloud provider.
     * <p>
     * The machines returned by the 'machine provisioner' will be <b>shared</b> by other processing
     * unit instances with the <u>same</u> <b>sharingId</b>.
     * <p>
     * See also {@link DiscoveredMachineProvisioningConfig} for configuring deployment on a
     * non-virtualized environment. Machines are discovered if 'Grid Service Agents' are running on
     * them.
     * 
     * @see #dedicatedMachineProvisioning(ElasticMachineProvisioningConfig)
     */
    ElasticDeploymentTopology sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config);
    
    /**
     * Configure the server side bean that starts and stops machines automatically. For example, the
     * bean could delegate the request to a cloud provider.
     * <p>
     * The machines returned by the 'machine provisioner' will be shared by any other instance
     * using publicMachineProvisioning or any non-elastic processing unit instance.
     */
    ElasticDeploymentTopology publicMachineProvisioning(ElasticMachineProvisioningConfig config);
    
    /**
     * Specifies the the heap size per container (operating system process)
     * For example: 
     * memoryCapacityPerContainer(256,MemoryUnit.MEGABYTES) is equivalent to
     * commandLineArgument("-Xmx256m").commandLineArgument("-Xms256m")
     */
    ElasticDeploymentTopology memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit);
    
    /**
     * Specifies the the heap size per container (operating system process)
     * For example: 
     * memoryCapacityPerContainer("256m") is equivalent to
     * commandLineArgument("-Xmx256m").commandLineArgument("-Xms256m")
     */
    ElasticDeploymentTopology memoryCapacityPerContainer(String memoryCapacityPerContainer);

}
