package org.openspaces.admin.pu.elastic;

import java.io.File;

import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.isolation.DedicatedIsolation;
import org.openspaces.admin.pu.elastic.isolation.PublicIsolation;
import org.openspaces.admin.pu.elastic.isolation.SharedTenantIsolation;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Exposes internal advanced properties that are not public in super class {@link ElasticStatefulProcessingUnitDeployment}
 * 
 * @author itaif
 *
 */
public class AdvancedElasticStatefulProcessingUnitDeployment extends ElasticStatefulProcessingUnitDeployment {

    public AdvancedElasticStatefulProcessingUnitDeployment(File processingUnit) {
        super(processingUnit);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfBackups(int numberOfBackups) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfBackups(numberOfBackups);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfInstances(int numberOfInstances) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfInstances(numberOfInstances);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.maxInstancesPerMachine(maxInstancesPerMachine);
    }
   
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleBeanConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleBeanConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualMemoryCapacityScaleBeanConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(MemoryCapacityScaleBeanConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleBeanConfig beanConfig) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(beanConfig.getBeanClassName(),beanConfig.getProperties());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleBeanConfig beanConfig) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(beanConfig.getBeanClassName(), beanConfig.getProperties());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualMemoryCapacityScaleBeanConfig beanConfig) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(beanConfig.getBeanClassName(), beanConfig.getProperties());
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(MemoryCapacityScaleBeanConfig beanConfig) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.enableScaleStrategy(beanConfig.getBeanClassName(), beanConfig.getProperties());
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment name(String name) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.name(name);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment zone(String zone) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.zone(zone);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment setContextProperty(String key, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment useScript() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.useScript();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment overrideVmInputArguments() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.overrideVmInputArguments();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment vmInputArgument(String vmInputArgument) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.vmInputArgument(vmInputArgument);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
}
