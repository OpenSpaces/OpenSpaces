package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.topology.ElasticStatelessDeploymentTopology;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic processing unit deployment that does not contain a space.
 * 
 * @author itaif
 */
public class ElasticStatelessProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment implements ElasticStatelessDeploymentTopology{

    Map<String,String> scaleStrategy;
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit name 
     * (should exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatelessProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    /**
     * Constructs a stateless processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatelessProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    public ElasticStatelessDeploymentTopology scale(EagerScaleConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticStatelessDeploymentTopology scale(ManualContainersScaleConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticStatelessProcessingUnitDeployment scale(ManualContainersScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticStatelessProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }
    
    public ElasticStatelessProcessingUnitDeployment name(String name) {
        return (ElasticStatelessProcessingUnitDeployment) super.name(name);
    }
/* NOT IMPLEMENTED YET
    public ElasticStatelessProcessingUnitDeployment zone(String zone) {
        return (ElasticStatelessProcessingUnitDeployment) super.zone(zone);
    }
*/
    public ElasticStatelessProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.setContextProperty(key, value);
    }


    public ElasticStatelessProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatelessProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticStatelessProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public ElasticStatelessProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userName, password);
    }
/* NOT IMPLEMENTED
    public ElasticStatelessProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticStatelessProcessingUnitDeployment) super.isolation(isolation);
    }

    public ElasticStatelessProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticStatelessProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticStatelessProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticStatelessProcessingUnitDeployment) super.isolation(isolation);
    }
*/    
    public ElasticStatelessProcessingUnitDeployment useScript() {
        return (ElasticStatelessProcessingUnitDeployment) super.useScript();
    }

    public ElasticStatelessProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatelessProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public ElasticStatelessProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (ElasticStatelessProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticStatelessProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticStatelessProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        return (ElasticStatelessProcessingUnitDeployment) super.machineProvisioning(config);
    }

    
}