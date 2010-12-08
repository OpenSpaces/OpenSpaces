package org.openspaces.admin.space;

import java.util.Map;

import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticDataGridDeployment implements ElasticStatefulDeploymentTopology {

    private final ElasticStatefulProcessingUnitDeployment deployment;
    private final String spaceName;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticDataGridDeployment(String spaceName) {
        this.spaceName = spaceName;
        this.deployment = new ElasticStatefulProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }

    public ElasticDataGridDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        deployment.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public ElasticDataGridDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        deployment.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticDataGridDeployment minMemoryCapacity(int minMemoryCapacity, MemoryUnit unit) {
        deployment.minMemoryCapacity(minMemoryCapacity,unit);
        return this;
    }

    public ElasticDataGridDeployment minMemoryCapacity(String minMemoryCapacity) {
        deployment.minMemoryCapacity(minMemoryCapacity);
        return this;
    }
    
    public ElasticDataGridDeployment enableScaleStrategy(EagerScaleBeanConfigurer strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }

    public ElasticDataGridDeployment enableScaleStrategy(ManualContainersScaleBeanConfigurer strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }

    public ElasticDataGridDeployment enableScaleStrategy(ManualMemoryCapacityScaleBeanConfigurer strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }

    public ElasticDataGridDeployment enableScaleStrategy(MemoryCapacityScaleBeanConfigurer strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment enableScaleStrategy(EagerScaleBeanConfig strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }

    public ElasticDataGridDeployment enableScaleStrategy(ManualContainersScaleBeanConfig strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }

    public ElasticDataGridDeployment enableScaleStrategy(ManualMemoryCapacityScaleBeanConfig strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment enableScaleStrategy(MemoryCapacityScaleConfig strategy) {
        deployment.enableScaleStrategy(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    public ElasticDataGridDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }

    public ElasticDataGridDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ElasticDataGridDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticDataGridDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticDataGridDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    public ElasticDataGridDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticDataGridDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticDataGridDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticDataGridDeployment useScript() {
        deployment.useScript();
        return this;
    }

    public ElasticDataGridDeployment overrideVmInputArguments() {
        deployment.overrideVmInputArguments();
        return this;
    }

    public ElasticDataGridDeployment vmInputArgument(String vmInputArgument) {
        deployment.vmInputArgument(vmInputArgument);
        return this;
    }

    public ElasticDataGridDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticDataGridDeployment highlyAvailable(boolean highlyAvailable) {
        deployment.highlyAvailable(highlyAvailable);
        return this;
    }

    public ElasticDataGridDeployment machinePool(String beanClassName, Map<String, String> beanProperties) {
        deployment.machinePool(beanClassName, beanProperties);
        return this;
    }

    public ProcessingUnitDeployment toProcessingUnitDeployment() {
       return deployment.toProcessingUnitDeployment();
    }
}
