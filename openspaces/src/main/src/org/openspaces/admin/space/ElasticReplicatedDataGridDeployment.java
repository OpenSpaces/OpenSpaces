package org.openspaces.admin.space;

import java.util.Map;

import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticReplicatedDeploymentTopology;

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

    public ElasticReplicatedDataGridDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }

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
    
    public ElasticReplicatedDataGridDeployment useScript() {
        deployment.useScript();
        return this;
    }

    public ElasticReplicatedDataGridDeployment overrideVmInputArguments() {
        deployment.overrideVmInputArguments();
        return this;
    }

    public ElasticReplicatedDataGridDeployment vmInputArgument(String vmInputArgument) {
        deployment.vmInputArgument(vmInputArgument);
        return this;
    }

    public ElasticReplicatedDataGridDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticReplicatedDataGridDeployment machinePool(String beanClassName, Map<String, String> beanProperties) {
        deployment.machinePool(beanClassName, beanProperties);
        return this;
    }

}
