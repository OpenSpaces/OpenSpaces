package org.openspaces.admin.memcached;

import java.util.Map;

import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.pu.container.servicegrid.deploy.MemcachedDeploy;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticMemcachedDeployment implements ElasticStatefulDeploymentTopology {

    private final ElasticStatefulProcessingUnitDeployment deployment;
    private final String spaceUrl;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticMemcachedDeployment(String spaceUrl) {
        this.spaceUrl = spaceUrl;
        this.deployment = new ElasticStatefulProcessingUnitDeployment("/templates/memcached");
        this.deployment.name(MemcachedDeploy.extractName(spaceUrl) + "-memcached");
        this.deployment.setContextProperty("url", spaceUrl);
    }

    public ElasticMemcachedDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        deployment.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public ElasticMemcachedDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        deployment.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticMemcachedDeployment minMemoryCapacity(int minMemoryCapacity, MemoryUnit unit) {
        deployment.minMemoryCapacity(minMemoryCapacity,unit);
        return this;
    }

    public ElasticMemcachedDeployment minMemoryCapacity(String minMemoryCapacity) {
        deployment.minMemoryCapacity(minMemoryCapacity);
        return this;
    }
    
    public ElasticMemcachedDeployment scale(EagerScaleBeanConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualContainersScaleBeanConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualMemoryCapacityScaleBeanConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(MemoryCapacityScaleBeanConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment scale(EagerScaleBeanConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualContainersScaleBeanConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualMemoryCapacityScaleBeanConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment scale(MemoryCapacityScaleBeanConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    public ElasticMemcachedDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }

    public ElasticMemcachedDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ElasticMemcachedDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticMemcachedDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticMemcachedDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    public ElasticMemcachedDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticMemcachedDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticMemcachedDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticMemcachedDeployment useScript() {
        deployment.useScript();
        return this;
    }

    public ElasticMemcachedDeployment overrideVmInputArguments() {
        deployment.overrideVmInputArguments();
        return this;
    }

    public ElasticMemcachedDeployment vmInputArgument(String vmInputArgument) {
        deployment.vmInputArgument(vmInputArgument);
        return this;
    }

    public ElasticMemcachedDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticMemcachedDeployment highlyAvailable(boolean highlyAvailable) {
        deployment.highlyAvailable(highlyAvailable);
        return this;
    }

    public ElasticMemcachedDeployment machinePool(String beanClassName, Map<String, String> beanProperties) {
        deployment.machinePool(beanClassName, beanProperties);
        return this;
    }
    
}
