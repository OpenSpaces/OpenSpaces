package org.openspaces.admin.memcached;

import java.util.Map;

import org.openspaces.admin.pu.elastic.ElasticReplicatedProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticReplicatedDeploymentTopology;
import org.openspaces.pu.container.servicegrid.deploy.MemcachedDeploy;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticReplicatedMemcachedDeployment implements ElasticReplicatedDeploymentTopology {

    private final ElasticReplicatedProcessingUnitDeployment deployment;
    private final String spaceUrl;
    private int numberOfContainers;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticReplicatedMemcachedDeployment(String spaceUrl) {
        this.spaceUrl = spaceUrl;
        this.deployment = new ElasticReplicatedProcessingUnitDeployment("/templates/memcached");
        this.deployment.name(MemcachedDeploy.extractName(spaceUrl) + "-memcached");
        this.deployment.setContextProperty("url", spaceUrl);
    }


    public ElasticReplicatedMemcachedDeployment numberOfContainers(int numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
        return this;
    }
    
    public ElasticReplicatedMemcachedDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticReplicatedMemcachedDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticReplicatedMemcachedDeployment useScript() {
        deployment.useScript();
        return this;
    }

    public ElasticReplicatedMemcachedDeployment overrideVmInputArguments() {
        deployment.overrideVmInputArguments();
        return this;
    }

    public ElasticReplicatedMemcachedDeployment vmInputArgument(String vmInputArgument) {
        deployment.vmInputArgument(vmInputArgument);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticReplicatedMemcachedDeployment machinePool(String beanClassName, Map<String, String> beanProperties) {
        deployment.machinePool(beanClassName, beanProperties);
        return this;
    }

}
