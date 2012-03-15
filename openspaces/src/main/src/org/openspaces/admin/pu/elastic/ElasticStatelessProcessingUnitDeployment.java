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

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.dependency.ProcessingUnitDeploymentDependenciesConfigurer;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.topology.ElasticStatelessDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic processing unit deployment that does not contain a space.
 * 
 * @author itaif
 */
public class ElasticStatelessProcessingUnitDeployment 
    extends AbstractElasticProcessingUnitDeployment
    implements ElasticStatelessDeploymentTopology<ProcessingUnitDependency> {
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit name 
     * (should exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatelessProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatelessProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }
    
    @Override
    public ElasticStatelessProcessingUnitDeployment name(String name) {
        return (ElasticStatelessProcessingUnitDeployment) super.name(name);
    }

    /**
     * @see #addContextProperty(String,String)
     */
    @Deprecated
    public ElasticStatelessProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticStatelessProcessingUnitDeployment) addContextProperty(key, value);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatelessProcessingUnitDeployment) super.secured(secured);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userDetails);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userName, password);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticStatelessProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatelessProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return addCommandLineArgument(vmInputArgument);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    @Override
    public ElasticStatelessProcessingUnitDeployment addCommandLineArgument(String vmInputArgument) {
        return (ElasticStatelessProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment addEnvironmentVariable(String name, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.environmentVariable(name, value);
    }
 
    @Override
    public ElasticStatelessProcessingUnitDeployment addContextProperty(String key, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.addContextProperty(key, value);
    }
    
    @Override
    public ElasticStatelessProcessingUnitDeployment dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config) {
        return (ElasticStatelessProcessingUnitDeployment) super.machineProvisioning(config, null);
    }
    
    @Override
    public ElasticStatelessProcessingUnitDeployment sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId can't be null");
        }
        return (ElasticStatelessProcessingUnitDeployment) super.machineProvisioning(config, sharingId);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment addDependency(String requiredProcessingUnitName) {
       addDependencies(new ProcessingUnitDeploymentDependenciesConfigurer().dependsOnDeployed(requiredProcessingUnitName).create());
       return this;
    }
 
    @Override
    public ElasticStatelessProcessingUnitDeployment addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
        super.addDependencies(detailedDependencies);
        return this; 
    }
    
    public ProcessingUnitDeployment toProcessingUnitDeployment(Admin admin) {

        ProcessingUnitDeployment deployment = super.toProcessingUnitDeployment();
                
        // disallow two instances to deploy on same Container
        deployment.maxInstancesPerVM(1);
        
        // allow any number of instances to deploy on same Machine
        deployment.maxInstancesPerMachine(0);
          
        return deployment;
    }
}
