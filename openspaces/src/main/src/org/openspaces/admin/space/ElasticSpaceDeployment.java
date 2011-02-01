package org.openspaces.admin.space;

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

    public ElasticSpaceDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        deployment.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public ElasticSpaceDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        deployment.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticSpaceDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public ElasticSpaceDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    public ElasticSpaceDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        deployment.maxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
       
    public ElasticSpaceDeployment scale(EagerScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticSpaceDeployment scale(ManualCapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticSpaceDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    public ElasticSpaceDeployment addContextProperty(String key, String value) {
        deployment.addContextProperty(key, value);
        return this;
    }

    public ElasticSpaceDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticSpaceDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticSpaceDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    /*UNIMPLEMENTED
    public ElasticSpaceDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticSpaceDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticSpaceDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    */
    
    public ElasticSpaceDeployment useScriptToStartContainer() {
        deployment.useScriptToStartContainer();
        return this;
    }

    public ElasticSpaceDeployment overrideCommandLineArguments() {
        deployment.overrideCommandLineArguments();
        return this;
    }

    public ElasticSpaceDeployment commandLineArgument(String commandLineArgument) {
        return addCommandLineArgument(commandLineArgument);
    }
    
    public ElasticSpaceDeployment addCommandLineArgument(String commandLineArgument) {
        deployment.commandLineArgument(commandLineArgument);
        return this;
    }

    public ElasticSpaceDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    public ElasticSpaceDeployment addEnvironmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticSpaceDeployment highlyAvailable(boolean highlyAvailable) {
        deployment.highlyAvailable(highlyAvailable);
        return this;
    }

    public ElasticSpaceDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        deployment.machineProvisioning(config);
        return this;
    }

    public ElasticSpaceDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        deployment.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
        return this;
    }

    public ElasticSpaceDeployment numberOfPartitions(int numberOfPartitions) {
        deployment.numberOfPartitions(numberOfPartitions);
        return this;
    }

    public ElasticSpaceDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        deployment.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }

    public ElasticSpaceDeployment singleMachineDeployment() {
        deployment.singleMachineDeployment();
        return this;
    }
   
    public ElasticStatefulProcessingUnitDeployment toElasticStatefulProcessingUnitDeployment() {
       return deployment;
    }
}
