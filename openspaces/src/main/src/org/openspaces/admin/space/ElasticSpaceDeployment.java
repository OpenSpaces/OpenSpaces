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

import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
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

public class ElasticSpaceDeployment extends AbstractElasticProcessingUnitDeployment 
    implements ElasticStatefulDeploymentTopology , AdvancedStatefulDeploymentTopology{

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticSpaceDeployment(String spaceName) {
        super(new ElasticSpaceConfig());
        getConfig().setName(spaceName);
    }

    @Override
    public ElasticSpaceDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        getConfig().setMaximumMemoryCapacityInMB(unit.toMegaBytes(maxMemoryCapacity));
        return this;
    }

    @Override
    public ElasticSpaceDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        getConfig().setMaximumMemoryCapacityInMB(MemoryUnit.toMegaBytes(maxMemoryCapacity));
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    @Override
    public ElasticSpaceDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        getConfig().setMaxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
       
    @Override
    public ElasticSpaceDeployment scale(EagerScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }

    @Override
    public ElasticSpaceDeployment scale(ManualCapacityScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public ElasticSpaceDeployment addContextProperty(String key, String value) {
        super.addContextProperty(key, value);
        return this;
    }

    @Override
    public ElasticSpaceDeployment secured(boolean secured) {
        super.secured(secured);
        return this;
    }

    @Override
    public ElasticSpaceDeployment userDetails(UserDetails userDetails) {
        super.userDetails(userDetails);
        return this;
    }

    @Override
    public ElasticSpaceDeployment userDetails(String userName, String password) {
        super.userDetails(userName, password);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment useScriptToStartContainer() {
        super.useScriptToStartContainer();
        return this;
    }

    @Override
    public ElasticSpaceDeployment overrideCommandLineArguments() {
        super.overrideCommandLineArguments();
        return this;
    }

    @Override
    public ElasticSpaceDeployment commandLineArgument(String commandLineArgument) {
        return addCommandLineArgument(commandLineArgument);
    }
    
    @Override
    public ElasticSpaceDeployment addCommandLineArgument(String commandLineArgument) {
        super.commandLineArgument(commandLineArgument);
        return this;
    }

    @Override
    public ElasticSpaceDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    @Override
    public ElasticSpaceDeployment addEnvironmentVariable(String name, String value) {
        super.environmentVariable(name, value);
        return this;
    }

    @Override
    public ElasticSpaceDeployment highlyAvailable(boolean highlyAvailable) {
        numberOfBackupsPerPartition(highlyAvailable ? 1 : 0);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config) {
        machineProvisioning(config, null);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId can't be null");
        }
        machineProvisioning(config, sharingId);
        return this;
    }

    @Override
    public ElasticSpaceDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        getConfig().setNumberOfBackupInstancesPerPartition(numberOfBackupsPerPartition);
        return this;
    }

    @Override
    public ElasticSpaceDeployment numberOfPartitions(int numberOfPartitions) {
        getConfig().setNumberOfPartitions(numberOfPartitions);
        return this;
    }
    
    /**
     * @deprecated since 8.0.6
     * @see ElasticMachineProvisioningConfig#getMinimumNumberOfCpuCoresPerMachine()
     */
    @Deprecated
    public ElasticSpaceDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        getConfig().setMinNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }
    
    @Override
    public ElasticSpaceDeployment singleMachineDeployment() {
        getConfig().setMaxProcessingUnitInstancesFromSamePartitionPerMachine(0);
        return this;
    }

    @Override
    public ElasticSpaceDeployment addDependency(String requiredProcessingUnitName) {
       addDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployed(requiredProcessingUnitName).create());
       return this;
    }

    @Override
    public ElasticSpaceDeployment addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
        super.addDependencies(detailedDependencies);
        return this;
    }

    @Override
    public ElasticSpaceConfig create() {
        return getConfig();
    }
    
    @Override
    public ElasticSpaceConfig getConfig() {
        return (ElasticSpaceConfig) super.getConfig();
    }
}
