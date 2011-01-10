package org.openspaces.admin.space;

import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticReplicatedDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticReplicatedDataGridDeployment implements ElasticReplicatedDeploymentTopology {

    private final ElasticStatefulProcessingUnitDeployment deployment;
    private final String spaceName;
    private int numberOfContainers;

    /**
     * Constructs a new data grid deployment with the specified name.
     */
    public ElasticReplicatedDataGridDeployment(String name) {
        this.spaceName = name;
        this.deployment = new ElasticStatefulProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }

    public ElasticReplicatedDataGridDeployment numberOfContainers(int numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
        return this;
    }
    
    public ElasticReplicatedDataGridDeployment name(String name) {
        deployment.name(name);
        return this;
    }
/* NOT IMPLEMENTED
    public ElasticReplicatedDataGridDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }
*/
    public ElasticReplicatedDataGridDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ElasticReplicatedDataGridDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticReplicatedDataGridDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticReplicatedDataGridDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    /* NOT IMPLEMENTED
    public ElasticReplicatedDataGridDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticReplicatedDataGridDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticReplicatedDataGridDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    */
    
    public ElasticReplicatedDataGridDeployment useScriptToStartContainer() {
        deployment.useScriptToStartContainer();
        return this;
    }

    public ElasticReplicatedDataGridDeployment overrideCommandLineArguments() {
        deployment.overrideCommandLineArguments();
        return this;
    }

    public ElasticReplicatedDataGridDeployment commandLineArgument(String vmInputArgument) {
        deployment.commandLineArgument(vmInputArgument);
        return this;
    }

    public ElasticReplicatedDataGridDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticReplicatedDataGridDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        deployment.machineProvisioning(config);
        return this;
    }

    public ElasticDeploymentTopology memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer, unit);
        return this;
    }

    public ElasticDeploymentTopology memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

    
}
