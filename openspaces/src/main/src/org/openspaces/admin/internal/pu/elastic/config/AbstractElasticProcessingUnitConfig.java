/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.admin.internal.pu.elastic.config;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.grid.gsa.GSProcessRestartOnExit;
import com.gigaspaces.security.directory.UserDetails;

/**
 * @author itaif
 * @since 9.0.1
 */
public class AbstractElasticProcessingUnitConfig {

    private String processingUnit;
    private String name;
    private StringProperties contextProperties = new StringProperties();
    private StringProperties defaultContextProperties = new StringProperties();
    private UserDetails userDetails;
    private boolean secured;
    private Map<String,String> elasticProperties = new HashMap<String,String>();
    private InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies = new DefaultProcessingUnitDependencies();
    private ElasticMachineProvisioningConfig machineProvisioning;
    private ScaleStrategyConfig scaleStrategy;;

    private GridServiceContainerConfig getGridServiceContainerConfig() {
        return new GridServiceContainerConfig(getElasticProperties());
    }
    
    private ElasticMachineIsolationConfig getElasticMachineIsolationConfig() {
        return new ElasticMachineIsolationConfig(getElasticProperties());
    }
    
    private MachineProvisioningBeanPropertiesManager getMachineProvisioningBeanPropertiesManager() {
        return new MachineProvisioningBeanPropertiesManager(getElasticProperties());
    }
    
    private ScaleStrategyBeanPropertiesManager getScaleStrategyBeanPropertiesManager() {
        return new ScaleStrategyBeanPropertiesManager(getElasticProperties());
    }

    public String getProcessingUnit() {
        return processingUnit;
    }

    public void setProcessingUnit(String processingUnit) {
        this.processingUnit = processingUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringProperties getContextProperties() {
        return contextProperties;
    }

    public void setContextProperties(StringProperties contextProperties) {
        this.contextProperties = contextProperties;
    }

    public StringProperties getDefaultContextProperties() {
        return defaultContextProperties;
    }

    public void setDefaultContextProperties(StringProperties defaultContextProperties) {
        this.defaultContextProperties = defaultContextProperties;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public Map<String,String> getElasticProperties() {
        return elasticProperties;
    }

    public void setElasticProperties(Map<String,String> elasticProperties) {
        this.elasticProperties = elasticProperties;
    }

    public InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void setUseScript(boolean useScript) {
        getGridServiceContainerConfig().setUseScript(useScript);
    }
    
    public boolean getUseScript() {
        return getGridServiceContainerConfig().getUseScript();
    }

    public void setOverrideCommandLineArguments(boolean overrideCommandLineArguments) {
        getGridServiceContainerConfig().setOverrideCommandLineArguments(overrideCommandLineArguments);
    }
    
    public boolean getOverrideCommandLineArguments() {
        return getGridServiceContainerConfig().getOverrideCommandLineArguments();
    }

    public void setMaximumMemoryCapacityInMB(long memoryInMB) {
        getGridServiceContainerConfig().setMaximumMemoryCapacityInMB(memoryInMB);        
    }
    
    public long getMaximumMemoryCapacityInMB() {
        return getGridServiceContainerConfig().getMaximumMemoryCapacityInMB();        
    }

    public String[] getCommandLineArguments() {
        return getGridServiceContainerConfig().getCommandLineArguments();
    }
    
    public void setCommandLineArguments(String[] commandLineArguments) {
        getGridServiceContainerConfig().setCommandLineArguments(commandLineArguments);
    }

    public Map<String, String> getEnvironmentVariables() {
        return getGridServiceContainerConfig().getEnvironmentVariables();
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        getGridServiceContainerConfig().setEnvironmentVariables(environmentVariables);
    }

    public void setDedicatedIsolation() {
        getElasticMachineIsolationConfig().setDedicated();
    }
    
    public boolean getDedicatedIsolationConfig() {
        return getElasticMachineIsolationConfig().isDedicatedIsolation();
    }

    public void setSharedIsolation(String sharingId) {
        getElasticMachineIsolationConfig().setSharingId(sharingId);
    }
    
    public String getSharedIsolation() {
        return getElasticMachineIsolationConfig().getSharingId();
    }

    public void setMachineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig) {
        if ((machineProvisioningConfig.getGridServiceAgentZones() == null || machineProvisioningConfig.getGridServiceAgentZones().length == 0) && machineProvisioningConfig.isGridServiceAgentZoneMandatory()) {
            throw new IllegalArgumentException("isGridServiceAgentZoneMandatory returns true, but no Grid Service Agent zone is specified.");
        }
        this.machineProvisioning = machineProvisioningConfig;
    }

    public ProcessingUnitDeployment toProcessingUnitDeployment() {
        GridServiceContainerConfig containerConfig = getGridServiceContainerConfig();
        if (containerConfig.getMaximumMemoryCapacityInMB() <= 0 && containerConfig.getMaximumJavaHeapSizeInMB() <=0) {
            throw new IllegalArgumentException("maximumMemoryCapacity or Xmx commandline must be defined.");
        }
        else if (containerConfig.getMaximumMemoryCapacityInMB() <= 0 && containerConfig.getMaximumJavaHeapSizeInMB() > 0) {
            // inject Xmx into maximumMemoryCapacityInMB
            containerConfig.setMaximumMemoryCapacityInMB(containerConfig.getMaximumJavaHeapSizeInMB());
        }
        else if (containerConfig.getMaximumMemoryCapacityInMB() > 0 && containerConfig.getMaximumJavaHeapSizeInMB() <= 0) {
            // inject maximumMemoryCapacityInMB into Xmx
            containerConfig.addMaximumJavaHeapSizeInMBCommandLineArgument(containerConfig.getMaximumMemoryCapacityInMB());
        }
        else if (containerConfig.getMaximumMemoryCapacityInMB() < containerConfig.getMaximumJavaHeapSizeInMB() ) {
            throw new IllegalArgumentException("maximumMemoryCapacity cannot be less than Xmx commandline argument.");
        }
        
        if (containerConfig.getMinimumJavaHeapSizeInMB() <= 0) {
            //inject Xmx into Xms
            containerConfig.addMinimumJavaHeapSizeInMBCommandLineArgument(containerConfig.getMaximumJavaHeapSizeInMB());
        }
        else if (containerConfig.getMinimumJavaHeapSizeInMB() > containerConfig.getMaximumJavaHeapSizeInMB() ) {
            throw new IllegalArgumentException("Xmx commandline argument "+ containerConfig.getMaximumJavaHeapSizeInMB() + "MB cannot be less than Xms commandline argument " + containerConfig.getMinimumJavaHeapSizeInMB() +"MB.");
        }
        
        // ESM takes care of GSC restart, no need for GSA to restart GSC
        containerConfig.setRestartOnExit(GSProcessRestartOnExit.NEVER); 
        
        if (machineProvisioning == null) {
            machineProvisioning = new DiscoveredMachineProvisioningConfig();
        }
        
        if (scaleStrategy == null) {
            scaleStrategy = new ManualCapacityScaleConfig();
        }
        
        if (machineProvisioning instanceof EagerScaleConfig && 
            !(scaleStrategy instanceof DiscoveredMachineProvisioningConfig)) {
            
            throw new IllegalArgumentException("Eager scale does not support " + machineProvisioning.getClass() + " machine provisioning. Remove machineProvisioning or use DiscoveredMachineProvisioningConfig() instead.");
         }

        enableBean(getMachineProvisioningBeanPropertiesManager(), machineProvisioning);
        enableBean(getScaleStrategyBeanPropertiesManager(), scaleStrategy);
            
        ProcessingUnitDeployment deployment = 
            new ProcessingUnitDeployment(this.processingUnit);
        
        if (this.name != null) {
            deployment.name(name);
        }
        
        if (this.secured) {
            deployment.secured(secured);
        }
        
        if (this.userDetails != null) {
            deployment.userDetails(userDetails);
        }

        String containerZone = getDefaultZone();
            
        deployment.addZone(containerZone);
        commandLineArgument("-Dcom.gs.zones=" + containerZone);
    
        // context properties defined by the user overrides the 
        // default context properties defined by the derived class.
        Map<String,String> context = defaultContextProperties.getProperties();
        context.putAll(contextProperties.getProperties());
        for (Map.Entry<String,String> entry : context.entrySet()) {
            deployment.setContextProperty(entry.getKey(), entry.getValue());
        }

        for (String key : elasticProperties.keySet()) {
            deployment.setElasticProperty(key, elasticProperties.get(key));
        }
        
        deployment.setDependencies(dependencies);
        
        return deployment;
    }

    public void setScaleStrategy(ScaleStrategyConfig scaleStrategy) {
        this.scaleStrategy= scaleStrategy;
    }
    
    public ScaleStrategyConfig getScaleStrategy() {
        return this.scaleStrategy;
    }

}
