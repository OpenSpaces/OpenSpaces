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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDeploymentDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

import com.gigaspaces.grid.gsa.GSProcessRestartOnExit;

/**
 * @author itaif
 * @since 9.0.1
 */
public class AbstractElasticProcessingUnitConfig {

    private String processingUnit;
    private String name;
    private Map<String,String> contextProperties = new HashMap<String,String> ();
    private Map<String,String> defaultContextProperties = new HashMap<String,String> ();
    private UserDetailsConfig userDetails;
    private Boolean secured;
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

    public Map<String,String> getContextProperties() {
        return contextProperties;
    }

    public void setContextProperties(Map<String,String> contextProperties) {
        this.contextProperties = contextProperties;
    }
    
    protected void addContextProperty(String key, String value) {
        this.contextProperties.put(key, value);
    }
    
    protected void addContextPropertyDefault(String key, String value) {
        this.defaultContextProperties.put(key, value);
    }

    public Map<String,String> getDefaultContextProperties() {
        return defaultContextProperties;
    }

    public void setDefaultContextProperties(Map<String,String> defaultContextProperties) {
        this.defaultContextProperties = defaultContextProperties;
    }

    public UserDetailsConfig getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetailsConfig userDetails) {
        this.userDetails = userDetails;
    }

    public Boolean getSecured() {
        return secured;
    }
    
    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public Map<String,String> getElasticProperties() {
        return elasticProperties;
    }

    @XmlTransient
    public void setElasticProperties(Map<String,String> elasticProperties) {
        this.elasticProperties = elasticProperties;
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

    public void setMemoryCapacityPerContainerInMB(long memoryInMB) {
        getGridServiceContainerConfig().setMaximumMemoryCapacityInMB(memoryInMB);        
    }
    
    public long getMemoryCapacityPerContainerInMB() {
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

    @XmlTransient
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

    public ElasticMachineProvisioningConfig getMachineProvisioning() {
        return machineProvisioning;
    }

    @XmlTransient
    public void setMachineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig) {
        if ((machineProvisioningConfig.getGridServiceAgentZones() == null || machineProvisioningConfig.getGridServiceAgentZones().length == 0) && machineProvisioningConfig.isGridServiceAgentZoneMandatory()) {
            throw new IllegalArgumentException("isGridServiceAgentZoneMandatory returns true, but no Grid Service Agent zone is specified.");
        }
        this.machineProvisioning = machineProvisioningConfig;
    }

    public ProcessingUnitConfig toProcessingUnitConfig() {
        
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
            
        ProcessingUnitConfig config = new ProcessingUnitConfig();
        config.setProcessingUnit(this.processingUnit);
        
        if (this.name != null) {
            config.setName(name);
        }
        
        if (this.secured) {
            config.setSecured(secured);
        }
        
        if (this.userDetails != null) {
            config.setUserDetails(userDetails);
        }

        String containerZone = getDefaultZone();
        addCommandLineArgument("-Dcom.gs.zones=" + containerZone);
        config.setZones(new String[]{containerZone});
    
        // context properties defined by the user overrides the 
        // default context properties defined by the derived class.
        Map<String,String> mergedContextProperties = defaultContextProperties;
        mergedContextProperties.putAll(contextProperties);
        config.setContextProperties(mergedContextProperties);
        config.setElasticProperties(elasticProperties);
        
        config.setDependencies(dependencies);
        
        return config;
    }

    public void setScaleStrategy(ScaleStrategyConfig scaleStrategy) {
        this.scaleStrategy= scaleStrategy;
    }
    
    public ScaleStrategyConfig getScaleStrategy() {
        return this.scaleStrategy;
    }


    private String getDefaultZone() {
        String zone = this.getName();
        if (zone == null) {
            //replace whitespaces
            zone = this.getProcessingUnit();
            
            //trim closing slash
            if (zone.endsWith("/") || zone.endsWith("\\")) {
                zone = zone.substring(0,zone.length()-1);
            }
            // pick directory/file name
            int seperatorIndex = Math.max(zone.lastIndexOf("/"),zone.lastIndexOf("\\"));
            if (seperatorIndex >= 0 && seperatorIndex < zone.length()-1 ) {
                zone = zone.substring(seperatorIndex+1,zone.length());
            }
            // remove file extension
            if (zone.endsWith(".zip") ||
                zone.endsWith(".jar") ||
                zone.endsWith(".war")) {

                zone = zone.substring(0, zone.length() - 4);
            }
        }
        
        zone = zone.replace(' ', '_');
        return zone;
    }

    public void addCommandLineArgument(String argument) {
        List<String> arguments = new ArrayList<String>(Arrays.asList(this.getCommandLineArguments()));
        arguments.add(argument);
        this.setCommandLineArguments(arguments.toArray(new String[arguments.size()]));
    }


    /**
     * Sets the elastic scale strategy 
     * @param config - the scale strategy bean configuration, or null to disable it.
     * @see ProcessingUnit#scale(org.openspaces.admin.pu.ElasticScaleStrategyConfig)
     */
    private static void enableBean(BeanConfigPropertiesManager propertiesManager, BeanConfig config) {
        propertiesManager.disableAllBeans();
        propertiesManager.setBeanConfig(config.getBeanClassName(), config.getProperties());
        propertiesManager.enableBean(config.getBeanClassName());
    }
    
    public InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> getDependencies() {
        return dependencies;
    }

    @XmlTransient
    public void setDependencies(InternalProcessingUnitDependencies<ProcessingUnitDependency,InternalProcessingUnitDependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public void setDeploymentDependencies(ProcessingUnitDependency[] dependencies) {
        
        DefaultProcessingUnitDeploymentDependencies deploymentDependencies = new DefaultProcessingUnitDeploymentDependencies();
        for (ProcessingUnitDependency dependency : dependencies) {
            deploymentDependencies.addDependency(dependency);
        }
        this.getDependencies().setDeploymentDependencies(deploymentDependencies);
    }
    
    public ProcessingUnitDependency[] getDeploymentDependencies() {
        List<ProcessingUnitDependency> dependenciesAsList = new ArrayList<ProcessingUnitDependency>();
        for (String name : this.getDependencies().getDeploymentDependencies().getRequiredProcessingUnitsNames()) {
            dependenciesAsList.add(this.getDependencies().getDeploymentDependencies().getDependencyByName(name));
        }
        return dependenciesAsList.toArray(new ProcessingUnitDependency[dependenciesAsList.size()]);
    }
}
