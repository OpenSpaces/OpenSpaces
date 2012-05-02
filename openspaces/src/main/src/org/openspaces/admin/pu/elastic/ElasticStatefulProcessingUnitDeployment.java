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
package org.openspaces.admin.pu.elastic;

import java.io.File;

import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.internal.pu.elastic.config.ElasticStatefulProcessingUnitConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.topology.AdvancedStatefulDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic deployment of a processing unit that with an embedded space..
 * 
 * The advantage of this topology is that the code can access the data without
 * the network/serialization overhead and that space events can be used as code triggers.
 * The disadvantage compared to a stateless processing unit is that the ratio between 
 * the minimum and maximum number of containers is limited.
 * 
 * @see ElasticStatefulProcessingUnitDeployment
 * 
 * @author itaif
 */
public class ElasticStatefulProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment 
    implements ElasticStatefulDeploymentTopology , AdvancedStatefulDeploymentTopology {
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(new ElasticStatefulProcessingUnitConfig());
        getConfig().setProcessingUnit(processingUnit);
    }
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatefulProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        getConfig().setMaxMemoryCapacityInMB(unit.toMegaBytes(maxMemoryCapacity));
        return this;
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        getConfig().setMaxMemoryCapacityInMB(MemoryUnit.toMegaBytes(maxMemoryCapacity));
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment highlyAvailable(boolean highlyAvailable) {
        numberOfBackupsPerPartition(highlyAvailable ? 1 : 0);
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        getConfig().setNumberOfBackupInstancesPerPartition(numberOfBackupsPerPartition);
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment numberOfPartitions(int numberOfPartitions) {
        getConfig().setNumberOfPartitions(numberOfPartitions);
        return this;
    }

    protected ElasticStatefulProcessingUnitDeployment maxProcessingUnitInstancesFromSamePartitionPerMachine(int maxProcessingUnitInstancesFromSamePartitionPerMachine) {
        getConfig().setMaxProcessingUnitInstancesFromSamePartitionPerMachine(maxProcessingUnitInstancesFromSamePartitionPerMachine);
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        getConfig().setMaxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }

    /**
     * @see ElasticMachineProvisioningConfig#getMinimumNumberOfCpuCoresPerMachine()
     */
    @Deprecated
    public ElasticStatefulProcessingUnitDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        getConfig().setMinNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment singleMachineDeployment() {
        this.maxProcessingUnitInstancesFromSamePartitionPerMachine(0);
        return this;
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment name(String name) {
        return (ElasticStatefulProcessingUnitDeployment) super.name(name);
    }
   
    @Override
    public ElasticStatefulProcessingUnitDeployment addContextProperty(String key, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.addContextProperty(key, value);
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticStatefulProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatefulProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return addCommandLineArgument(vmInputArgument);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment addCommandLineArgument(String vmInputArgument) {
        return (ElasticStatefulProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    @Override
    public ElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment addEnvironmentVariable(String name, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    @Override
    protected ElasticStatefulProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig, String sharingId) {
        if (machineProvisioningConfig == null) {
            throw new IllegalArgumentException("machineProvisioningConfig");
        }
        
        return (ElasticStatefulProcessingUnitDeployment) super.machineProvisioning(machineProvisioningConfig, sharingId);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config) {
        return machineProvisioning(config, null);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId can't be null");
        }
        return machineProvisioning(config, sharingId);
    }
    
    @Override
    public ElasticStatefulProcessingUnitDeployment addDependency(String requiredProcessingUnitName) {
        addDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployed(requiredProcessingUnitName).create());
        return this;
    }
 
    @Override
    public ElasticStatefulProcessingUnitDeployment addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
        return (ElasticStatefulProcessingUnitDeployment) super.addDependencies(detailedDependencies); 
    }

    public ElasticStatefulProcessingUnitConfig create() {
        return getConfig();
    }
    
    protected ElasticStatefulProcessingUnitConfig getConfig() {
        return (ElasticStatefulProcessingUnitConfig) super.getConfig();
    }
}
