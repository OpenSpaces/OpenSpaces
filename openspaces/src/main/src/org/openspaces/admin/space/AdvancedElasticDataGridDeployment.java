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

public class AdvancedElasticDataGridDeployment extends ElasticDataGridDeployment implements AdvancedStatefulDeploymentTopology {

    public AdvancedElasticDataGridDeployment(String spaceName) {
        super(spaceName);
    }
    
    public AdvancedElasticDataGridDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        return (AdvancedElasticDataGridDeployment) super.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
    }
    
    public AdvancedElasticDataGridDeployment numberOfPartitions(int numberOfPartitions) {
        return (AdvancedElasticDataGridDeployment) super.numberOfPartitions(numberOfPartitions);
    }

    public AdvancedElasticDataGridDeployment allowDeploymentOnSingleMachine() {
        super.allowDeploymentOnSingleMachine();
        return this;
    }

    public AdvancedElasticDataGridDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        return (AdvancedElasticDataGridDeployment) super.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
    }
    
    public AdvancedElasticDataGridDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        super.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public AdvancedElasticDataGridDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        super.maxMemoryCapacity(maxMemoryCapacity);
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
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticDataGridDeployment scale(ManualContainersScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticDataGridDeployment scale(ManualCapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticDataGridDeployment scale(CapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }
    
    public AdvancedElasticDataGridDeployment scale(EagerScaleConfig strategy) {
        return (AdvancedElasticDataGridDeployment) super.scale(strategy);
    }

    public AdvancedElasticDataGridDeployment scale(ManualContainersScaleConfig strategy) {
        return (AdvancedElasticDataGridDeployment) super.scale(strategy);
    }

    public AdvancedElasticDataGridDeployment scale(ManualCapacityScaleConfig strategy) {
        return (AdvancedElasticDataGridDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticDataGridDeployment scale(CapacityScaleConfig strategy) {
        return (AdvancedElasticDataGridDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticDataGridDeployment name(String name) {
        return (AdvancedElasticDataGridDeployment) super.name(name);
    }
/* NOT IMPLEMENTED YET
    public AdvancedElasticDataGridDeployment zone(String zone) {
        return (AdvancedElasticDataGridDeployment) super.zone(zone);
    }
    
    
    public AdvancedElasticDataGridDeployment isolation(DedicatedIsolation isolation) {
        return (AdvancedElasticDataGridDeployment) super.isolation(isolation);
    }

    public AdvancedElasticDataGridDeployment isolation(SharedTenantIsolation isolation) {
        return (AdvancedElasticDataGridDeployment) super.isolation(isolation);
    }
    
    public AdvancedElasticDataGridDeployment isolation(PublicIsolation isolation) {
        return (AdvancedElasticDataGridDeployment) super.isolation(isolation);
    }

*/
    public AdvancedElasticDataGridDeployment setContextProperty(String key, String value) {
        return (AdvancedElasticDataGridDeployment) super.setContextProperty(key, value);
    }

    public AdvancedElasticDataGridDeployment secured(boolean secured) {
        return (AdvancedElasticDataGridDeployment) super.secured(secured);
    }

    public AdvancedElasticDataGridDeployment userDetails(UserDetails userDetails) {
        return (AdvancedElasticDataGridDeployment) super.userDetails(userDetails);
    }

    public AdvancedElasticDataGridDeployment userDetails(String userName, String password) {
        return (AdvancedElasticDataGridDeployment) super.userDetails(userName, password);
    }
    
    public AdvancedElasticDataGridDeployment useScriptToStartContainer() {
        return (AdvancedElasticDataGridDeployment) super.useScriptToStartContainer();
    }

    public AdvancedElasticDataGridDeployment overrideCommandLineArguments() {
        return (AdvancedElasticDataGridDeployment) super.overrideCommandLineArguments();
    }

    public AdvancedElasticDataGridDeployment commandLineArgument(String vmInputArgument) {
        return (AdvancedElasticDataGridDeployment) super.commandLineArgument(vmInputArgument);
    }

    public AdvancedElasticDataGridDeployment environmentVariable(String name, String value) {
        return (AdvancedElasticDataGridDeployment) super.environmentVariable(name, value);
    }

}
