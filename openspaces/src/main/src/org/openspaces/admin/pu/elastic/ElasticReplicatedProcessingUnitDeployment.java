package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.topology.ElasticReplicatedDeploymentTopology;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticReplicatedProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment implements ElasticReplicatedDeploymentTopology {

    Map<String,String> scaleStrategy;
    private int numberOfContainers;
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit name 
     * (should exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticReplicatedProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticReplicatedProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }
    
    public ElasticReplicatedDeploymentTopology numberOfContainers(int numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
        return this;
    }
   
    public ElasticReplicatedDeploymentTopology enableScaleStrategy(ManualContainersScaleConfigurer configurer) {
        return enableScaleStrategy(configurer.getConfig());
    }
    
    public ElasticReplicatedProcessingUnitDeployment enableScaleStrategy(ManualContainersScaleConfig strategy) {
        return (ElasticReplicatedProcessingUnitDeployment) super.scale(strategy);
    }
   
    public ElasticReplicatedProcessingUnitDeployment name(String name) {
        return (ElasticReplicatedProcessingUnitDeployment) super.name(name);
    }
/* NOT IMPLEMENTED
    public ElasticReplicatedProcessingUnitDeployment zone(String zone) {
        return (ElasticReplicatedProcessingUnitDeployment) super.zone(zone);
    }
*/
    public ElasticReplicatedProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticReplicatedProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public ElasticReplicatedProcessingUnitDeployment secured(boolean secured) {
        return (ElasticReplicatedProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticReplicatedProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticReplicatedProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public ElasticReplicatedProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticReplicatedProcessingUnitDeployment) super.userDetails(userName, password);
    }
/* NOT IMPLEMENTED
    public ElasticReplicatedProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticReplicatedProcessingUnitDeployment) super.isolation(isolation);
    }

    public ElasticReplicatedProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticReplicatedProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticReplicatedProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticReplicatedProcessingUnitDeployment) super.isolation(isolation);
    }
*/    
    public ElasticReplicatedProcessingUnitDeployment useScript() {
        return (ElasticReplicatedProcessingUnitDeployment) super.useScript();
    }

    public ElasticReplicatedProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticReplicatedProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public ElasticReplicatedProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (ElasticReplicatedProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticReplicatedProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticReplicatedProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticReplicatedProcessingUnitDeployment machineProvisioning(BeanConfig config) {
        return (ElasticReplicatedProcessingUnitDeployment) super.machineProvisioning(config);
    }
}