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
* This Advanced version of {@link ElasticDataGridDeployment} allows 
* implementation related tweaking that might change in the future.
* 
* @see ElasticDataGridDeployment
* @author itaif
* @since 8.0
*/
public class AdvancedElasticDataGridDeployment extends ElasticDataGridDeployment implements AdvancedStatefulDeploymentTopology {

    public AdvancedElasticDataGridDeployment(String spaceName) {
        super(spaceName);
    }
    
    public AdvancedElasticDataGridDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        super.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment numberOfPartitions(int numberOfPartitions) {
        super.numberOfPartitions(numberOfPartitions);
        return this;
    }

    public AdvancedElasticDataGridDeployment allowDeploymentOnSingleMachine(boolean allowDeploymentOnSingleMachine) {
        super.allowDeploymentOnSingleMachine(allowDeploymentOnSingleMachine);
        return this;
    }
 
    public AdvancedElasticDataGridDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        super.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        super.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public AdvancedElasticDataGridDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        super.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticDataGridDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        super.maxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public AdvancedElasticDataGridDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        super.machineProvisioning(config);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment scale(EagerScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticDataGridDeployment scale(ManualContainersScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticDataGridDeployment scale(ManualCapacityScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }

    public AdvancedElasticDataGridDeployment scale(CapacityScaleConfigurer beanConfig) {
        scale(beanConfig.getConfig());
        return this;
    }
    
    public AdvancedElasticDataGridDeployment scale(EagerScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }

    public AdvancedElasticDataGridDeployment scale(ManualContainersScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }

    public AdvancedElasticDataGridDeployment scale(ManualCapacityScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment scale(CapacityScaleConfig strategy) {
        super.scale(strategy);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment name(String name) {
        super.name(name);
        return this;
    }

    public AdvancedElasticDataGridDeployment setContextProperty(String key, String value) {
        super.setContextProperty(key, value);
        return this;
    }

    public AdvancedElasticDataGridDeployment secured(boolean secured) {
        super.secured(secured);
        return this;
    }

    public AdvancedElasticDataGridDeployment userDetails(UserDetails userDetails) {
        super.userDetails(userDetails);
        return this;
    }

    public AdvancedElasticDataGridDeployment userDetails(String userName, String password) {
        super.userDetails(userName, password);
        return this;
    }
    
    public AdvancedElasticDataGridDeployment useScriptToStartContainer() {
        super.useScriptToStartContainer();
        return this;
    }

    public AdvancedElasticDataGridDeployment overrideCommandLineArguments() {
        super.overrideCommandLineArguments();
        return this;
    }

    public AdvancedElasticDataGridDeployment commandLineArgument(String vmInputArgument) {
        super.commandLineArgument(vmInputArgument);
        return this;
    }

    public AdvancedElasticDataGridDeployment environmentVariable(String name, String value) {
        super.environmentVariable(name, value);
        return this;
    }
}
