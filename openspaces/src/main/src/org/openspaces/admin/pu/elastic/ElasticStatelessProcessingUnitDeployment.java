package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.topology.AdvancedStatelessDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.admin.pu.elastic.topology.ElasticStatelessDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Defines an elastic processing unit deployment that does not contain a space.
 * 
 * @author itaif
 */
public class ElasticStatelessProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment
    implements ElasticStatelessDeploymentTopology, AdvancedStatelessDeploymentTopology {

    Map<String,String> scaleStrategy;
    private double minNumberOfCpuCoresPerMachine;
    
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

    public ElasticStatelessProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticStatelessProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatelessProcessingUnitDeployment) super.scale(strategy);
    }
    
    @Override
    public ElasticStatelessProcessingUnitDeployment name(String name) {
        return (ElasticStatelessProcessingUnitDeployment) super.name(name);
    }

    public ElasticStatelessProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.addContextProperty(key, value);
    }


    @Override
    public ElasticStatelessProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatelessProcessingUnitDeployment) super.secured(secured);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userDetails);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatelessProcessingUnitDeployment) super.userDetails(userName, password);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticStatelessProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatelessProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return addCommandLineArgument(vmInputArgument);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment environmentVariable(String name, String value) {
        return addEnvironmentVariable(name, value);
    }
    
    public ElasticStatelessProcessingUnitDeployment addCommandLineArgument(String vmInputArgument) {
        return (ElasticStatelessProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticStatelessProcessingUnitDeployment addEnvironmentVariable(String name, String value) {
        return (ElasticStatelessProcessingUnitDeployment) super.environmentVariable(name, value);
    }
 
    @Override
    public ElasticStatefulProcessingUnitDeployment addContextProperty(String key, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.addContextProperty(key, value);
    }
    
    public ElasticStatelessProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        return (ElasticStatelessProcessingUnitDeployment) dedicatedMachineProvisioning(config);
    }
    
    public ElasticDeploymentTopology dedicatedMachineProvisioning(ElasticMachineProvisioningConfig config) {
        return (ElasticStatelessProcessingUnitDeployment) super.machineProvisioning(config, null);
    }
    
    public ElasticDeploymentTopology sharedMachineProvisioning(String sharingId, ElasticMachineProvisioningConfig config) {
        if (sharingId == null) {
            throw new IllegalArgumentException("sharingId can't be null");
        }
        return (ElasticStatelessProcessingUnitDeployment) super.machineProvisioning(config, sharingId);
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    @Override
    public ElasticStatelessProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

    public ElasticStatelessProcessingUnitDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        this.minNumberOfCpuCoresPerMachine = minNumberOfCpuCoresPerMachine;
        return this;
    }
    
    public ProcessingUnitDeployment toProcessingUnitDeployment(Admin admin) {
        
        ProcessingUnitDeployment deployment = super.toProcessingUnitDeployment();
                
        // disallow two instances to deploy on same Container
        deployment.maxInstancesPerVM(1);
        
        // allow any number of instances to deploy on same Machine
        deployment.maxInstancesPerMachine(0);
          
        return deployment;
    }

}