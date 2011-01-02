package org.openspaces.admin.memcached;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfigurer;
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
    
    public ElasticMemcachedDeployment scale(EagerScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualContainersScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualCapacityScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(CapacityScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment scale(EagerScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualContainersScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticMemcachedDeployment scale(ManualCapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment scale(CapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticMemcachedDeployment name(String name) {
        deployment.name(name);
        return this;
    }
/* NOT IMPLEMENTED
    public ElasticMemcachedDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }
*/
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
/* NOT IMPLEMENTED
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
*/    
    public ElasticMemcachedDeployment useScript() {
        deployment.useScript();
        return this;
    }

    public ElasticMemcachedDeployment overrideCommandLineArguments() {
        deployment.overrideCommandLineArguments();
        return this;
    }

    public ElasticMemcachedDeployment commandLineArgument(String vmInputArgument) {
        deployment.commandLineArgument(vmInputArgument);
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

    public ElasticMemcachedDeployment machineProvisioning(BeanConfig config) {
        deployment.machineProvisioning(config);
        return this;
    }
}
