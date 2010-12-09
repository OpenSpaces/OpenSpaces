package org.openspaces.jee;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;
import org.openspaces.admin.pu.elastic.topology.ElasticWebDeploymentTopology;

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

    public ElasticWebProcessingUnitDeployment scale(EagerScaleBeanConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticWebProcessingUnitDeployment scale(ManualContainersScaleBeanConfigurer configurer) {
        return scale(configurer.getConfig());
    }
    
    public ElasticWebProcessingUnitDeployment scale(ManualContainersScaleBeanConfig strategy) {
        return (ElasticWebProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(), strategy.getProperties());
    }

    public ElasticWebProcessingUnitDeployment scale(EagerScaleBeanConfig strategy) {
        return (ElasticWebProcessingUnitDeployment) super.enableScaleStrategy(strategy.getBeanClassName(), strategy.getProperties());
    }
    
    public ElasticWebProcessingUnitDeployment name(String name) {
        return (ElasticWebProcessingUnitDeployment) super.name(name);
    }

    public ElasticWebProcessingUnitDeployment zone(String zone) {
        return (ElasticWebProcessingUnitDeployment) super.zone(zone);
    }

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

    public ElasticWebProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }

    public ElasticWebProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticWebProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticWebProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticWebProcessingUnitDeployment useScript() {
        return (ElasticWebProcessingUnitDeployment) super.useScript();
    }

    public ElasticWebProcessingUnitDeployment overrideVmInputArguments() {
        return (ElasticWebProcessingUnitDeployment) super.overrideVmInputArguments();
    }

    public ElasticWebProcessingUnitDeployment vmInputArgument(String vmInputArgument) {
        return (ElasticWebProcessingUnitDeployment) super.vmInputArgument(vmInputArgument);
    }

    public ElasticWebProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticWebProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticWebProcessingUnitDeployment machinePool(String beanClassName, Map<String,String> beanProperties) {
        return (ElasticWebProcessingUnitDeployment) super.machinePool(beanClassName, beanProperties);
    }
}
