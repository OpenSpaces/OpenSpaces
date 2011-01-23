package org.openspaces.admin.space;

import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.topology.AdvancedStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;
/*
* This Advanced version of {@link ElasticSpaceDeployment} allows 
* implementation related tweaking that might change in the future.
* 
* @see ElasticSpaceDeployment
* @author itaif
* @since 8.0
*/
public class AdvancedElasticSpaceDeployment extends ElasticSpaceDeployment implements AdvancedStatefulDeploymentTopology {

    public AdvancedElasticSpaceDeployment(String spaceName) {
        super(spaceName);
    }
    
    public AdvancedElasticSpaceDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        super.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment numberOfPartitions(int numberOfPartitions) {
        super.numberOfPartitions(numberOfPartitions);
        return this;
    }

    public AdvancedElasticSpaceDeployment singleMachineDeployment() {
        super.singleMachineDeployment();
        return this;
    }
 
    public AdvancedElasticSpaceDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        super.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        super.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public AdvancedElasticSpaceDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        super.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticSpaceDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        super.maxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public AdvancedElasticSpaceDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        super.machineProvisioning(config);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment scale(EagerScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticSpaceDeployment scale(ManualContainersScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticSpaceDeployment scale(ManualCapacityScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticSpaceDeployment scale(CapacityScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }
    
    public AdvancedElasticSpaceDeployment scale(EagerScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }

    public AdvancedElasticSpaceDeployment scale(ManualContainersScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }

    public AdvancedElasticSpaceDeployment scale(ManualCapacityScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment scale(CapacityScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment name(String name) {
        super.name(name);
        return this;
    }

    public AdvancedElasticSpaceDeployment setContextProperty(String key, String value) {
        super.setContextProperty(key, value);
        return this;
    }

    public AdvancedElasticSpaceDeployment secured(boolean secured) {
        super.secured(secured);
        return this;
    }

    public AdvancedElasticSpaceDeployment userDetails(UserDetails userDetails) {
        super.userDetails(userDetails);
        return this;
    }

    public AdvancedElasticSpaceDeployment userDetails(String userName, String password) {
        super.userDetails(userName, password);
        return this;
    }
    
    public AdvancedElasticSpaceDeployment useScriptToStartContainer() {
        super.useScriptToStartContainer();
        return this;
    }

    public AdvancedElasticSpaceDeployment overrideCommandLineArguments() {
        super.overrideCommandLineArguments();
        return this;
    }

    public AdvancedElasticSpaceDeployment commandLineArgument(String vmInputArgument) {
        super.commandLineArgument(vmInputArgument);
        return this;
    }

    public AdvancedElasticSpaceDeployment environmentVariable(String name, String value) {
        super.environmentVariable(name, value);
        return this;
    }
}
