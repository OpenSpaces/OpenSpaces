package org.openspaces.jee;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.topology.ElasticWebDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;


/**
 * Defines an elastic state-less processing unit deployment that embeds a Jetty web server.
 * 
 * @author itaif
 */
public class ElasticWebProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment implements ElasticWebDeploymentTopology {

    Map<String,String> scaleStrategy;
    
    /**
     * Constructs a web processing unit deployment based on the specified processing unit name 
     * (should exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticWebProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    /**
     * Constructs a web processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit war file or a directory).
     */
    public ElasticWebProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    public ElasticWebProcessingUnitDeployment scale(EagerScaleConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticWebProcessingUnitDeployment scale(ManualContainersScaleConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticWebProcessingUnitDeployment scale(ManualContainersScaleConfig strategy) {
        return (ElasticWebProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticWebProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticWebProcessingUnitDeployment) super.scale(strategy);
    }
    
    public ElasticWebProcessingUnitDeployment name(String name) {
        return (ElasticWebProcessingUnitDeployment) super.name(name);
    }
/* NOT IMPLEMENTED YET
    public ElasticWebProcessingUnitDeployment zone(String zone) {
        return (ElasticWebProcessingUnitDeployment) super.zone(zone);
    }


    public ElasticWebProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }

    public ElasticWebProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticWebProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }

*/
    public ElasticWebProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticWebProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public ElasticWebProcessingUnitDeployment secured(boolean secured) {
        return (ElasticWebProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticWebProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticWebProcessingUnitDeployment) super.userDetails(userDetails);

    }

    public ElasticWebProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticWebProcessingUnitDeployment) super.userDetails(userName, password);
    }
    
    public ElasticWebProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticWebProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    public ElasticWebProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticWebProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public ElasticWebProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (ElasticWebProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticWebProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticWebProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticWebProcessingUnitDeployment machineProvisioning(BeanConfig config) {
        return (ElasticWebProcessingUnitDeployment) super.machineProvisioning(config);
    }
    
    public ElasticWebProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public ElasticWebProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

}
