package org.openspaces.admin.pu.elastic;

import java.io.File;

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

/**
 * Exposes internal advanced properties that are not public in super class {@link ElasticStatefulProcessingUnitDeployment}
 * 
 * @author itaif
 *
 */
public class AdvancedElasticStatefulProcessingUnitDeployment extends ElasticStatefulProcessingUnitDeployment implements AdvancedStatefulDeploymentTopology {

    public AdvancedElasticStatefulProcessingUnitDeployment(File processingUnit) {
        super(processingUnit);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfPartitions(int numberOfPartitions) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfPartitions(numberOfPartitions);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment maxProcessingUnitInstancesFromSamePartitionPerMachine(int maxProcessingUnitInstancesFromSamePartitionPerMachine) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.maxProcessingUnitInstancesFromSamePartitionPerMachine(maxProcessingUnitInstancesFromSamePartitionPerMachine);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        super.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public AdvancedElasticStatefulProcessingUnitDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        super.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public AdvancedElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(CapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(CapacityScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment name(String name) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.name(name);
    }
/* NOT IMPLEMENTED YET
    public AdvancedElasticStatefulProcessingUnitDeployment zone(String zone) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.zone(zone);
    }
    
    
    public AdvancedElasticStatefulProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

*/
    public AdvancedElasticStatefulProcessingUnitDeployment setContextProperty(String key, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment useScriptToStartContainer() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment overrideCommandLineArguments() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }

}
