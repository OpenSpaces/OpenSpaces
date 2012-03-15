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
package org.openspaces.admin.space;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.topology.AdvancedStatefulDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic deployment of a partitioned data grid (space).
 * 
 * The advantage of partitioned topology is that the data can spread across different containers,
 * and is not limited by the size of each container.
 * 
 * The disadvantage compared to replicated topology is that there is only 1 read/write endpoint
 * for each data object (no concurrent reads from different containers for the same data).
 * 
 * @see ElasticSpaceDeployment
 * 
 * @author itaif
 * @since 8.0
 */

public class ElasticSpaceDeployment 
    implements ElasticStatefulDeploymentTopology , AdvancedStatefulDeploymentTopology{

    private final ElasticStatefulProcessingUnitDeployment deployment;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticSpaceDeployment(String spaceName) {
        this.deployment = new ElasticStatefulProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.addContextProperty("dataGridName", spaceName);
    }

    @Override
    public ElasticSpaceDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        deployment.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    @Override
    public ElasticSpaceDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        deployment.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    @Override
    public ElasticSpaceDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        deployment.maxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
       
    @Override
    public ElasticSpaceDeployment scale(EagerScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    @Override
    public ElasticSpaceDeployment scale(ManualCapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    @Override
    public ElasticSpaceDeployment addContextProperty(String key, String value) {
        deployment.addContextProperty(key, value);
        return this;
    }

    @Override
    public ElasticSpaceDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    @Override
    public ElasticSpaceDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    @Override
    public ElasticSpaceDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment useScriptToStartContainer() {
        deployment.useScriptToStartContainer();
        return this;
    }

    @Override
    public ElasticSpaceDeployment overrideCommandLineArguments() {
        deployment.overrideCommandLineArguments();
        return this;
    }

    @Override
    public ElasticSpaceDeployment commandLineArgument(String commandLineArgument) {
        return addCommandLineArgument(commandLineArgument);
    }
    
    @Override
    public ElasticSpaceDeployment addCommandLineArgument(String commandLineArgument) {
        deployment.commandLineArgument(commandLineArgument);
        return this;
    }

    @Override
    public ElasticSpaceDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    @Override
    public ElasticSpaceDeployment addEnvironmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    @Override
    public ElasticSpaceDeployment highlyAvailable(boolean highlyAvailable) {
        deployment.highlyAvailable(highlyAvailable);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config) {
        deployment.dedicatedMachineProvisioning(config);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config) {
        deployment.sharedMachineProvisioning(sharingId, config);
        return this;
    }

    @Override
    public ElasticSpaceDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        deployment.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
        return this;
    }

    @Override
    public ElasticSpaceDeployment numberOfPartitions(int numberOfPartitions) {
        deployment.numberOfPartitions(numberOfPartitions);
        return this;
    }
    
    /**
     * @deprecated since 8.0.6
     * @see ElasticMachineProvisioningConfig#getMinimumNumberOfCpuCoresPerMachine()
     */
    @Deprecated
    public ElasticSpaceDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        deployment.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment singleMachineDeployment() {
        deployment.singleMachineDeployment();
        return this;
    }

    @Override
    public ElasticSpaceDeployment addDependency(String requiredProcessingUnitName) {
       deployment.addDependency(requiredProcessingUnitName);
       return this;
    }

    @Override
    public ElasticSpaceDeployment addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
        deployment.addDependencies(detailedDependencies);
        return this;
    }
    
    @Override
    public ProcessingUnitDeployment toProcessingUnitDeployment(Admin admin) {
       return deployment.toProcessingUnitDeployment(admin);
    }
}
