/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.pu.elastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.internal.pu.dependency.DefaultProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependencies;
import org.openspaces.admin.internal.pu.dependency.InternalProcessingUnitDependency;
import org.openspaces.admin.internal.pu.dependency.ProcessingUnitDetailedDependencies;
import org.openspaces.admin.internal.pu.elastic.config.AbstractElasticProcessingUnitConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.grid.gsa.GSProcessRestartOnExit;
import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

import java.util.Arrays;

public abstract class AbstractElasticProcessingUnitDeployment {

    AbstractElasticProcessingUnitConfig config;
    
        
    public AbstractElasticProcessingUnitDeployment(AbstractElasticProcessingUnitConfig config, String processingUnit) {
        this.config = config;
        config.setProcessingUnit(processingUnit);
    }
        
    protected void addContextPropertyDefault(String key, String defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        config.getDefaultContextProperties().put(key,defaultValue);
    }
    
    protected AbstractElasticProcessingUnitDeployment addContextProperty(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        config.getContextProperties().put(key, value);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment secured(boolean secured) {
        config.setSecured(secured);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment name(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (config.getName() != null && !config.getName().equals(name)) {
            throw new IllegalStateException("Name is already defined to " + config.getName() + " and cannot be modified to " + name);
        }
        config.setName(name);
        return this;
    }


    protected AbstractElasticProcessingUnitDeployment userDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User details cannot be null");
        }
        if (config.getUserDetails() != null && !config.getUserDetails().equals(userDetails)) {
            throw new IllegalStateException("User details are already defined and cannot be modified.");
        }
        config.setUserDetails(userDetails);
        return this;
    }

   
    protected AbstractElasticProcessingUnitDeployment userDetails(String userName, String password) {
        userDetails(new User(userName,password));
        return this;       
    }

    /**
     * Will cause the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started using a script
     * and not a pure Java process.
     */
    protected AbstractElasticProcessingUnitDeployment useScriptToStartContainer() {
        config.setUseScript(true);
        return this;
    }

    /**
     * Will cause JVM options added using {@link #commandLineArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    protected AbstractElasticProcessingUnitDeployment overrideCommandLineArguments() {
        config.setOverrideCommandLineArguments(true);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        memoryCapacityPerContainer(MemoryUnit.MEGABYTES.convert(memoryCapacityPerContainer));
        return this;
    }

    private void memoryCapacityPerContainer(long memoryInMB) {
        config.setMaximumMemoryCapacityInMB(memoryInMB);
    }
    
    protected AbstractElasticProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        memoryCapacityPerContainer(unit.toMegaBytes(memoryCapacityPerContainer));
        return this;
    }
    
    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    protected AbstractElasticProcessingUnitDeployment commandLineArgument(String argument) {
        List<String> arguments = new ArrayList<String>(Arrays.asList(config.getCommandLineArguments()));
        arguments.add(argument);
        config.setCommandLineArguments(arguments.toArray(new String[arguments.size()]));
        return this;
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    protected AbstractElasticProcessingUnitDeployment environmentVariable(String name, String value) {
        Map<String,String> environmentVariables = config.getEnvironmentVariables();
        environmentVariables.put(name, value);
        config.setEnvironmentVariables(environmentVariables);
        return this;
    }
    
    protected AbstractElasticProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig, String sharingId) {
        
        if (sharingId == null) {
            config.setDedicatedIsolation();
        }
        else {
            config.setSharedIsolation(sharingId);
        }
        config.setMachineProvisioning(machineProvisioningConfig);
        return this;
    }    

    protected AbstractElasticProcessingUnitDeployment scale(ScaleStrategyConfig scaleStrategyConfig) {
        config.setScaleStrategy(scaleStrategyConfig);
        return this;
    }
    
    protected AbstractElasticProcessingUnitDeployment addDependencies(ProcessingUnitDetailedDependencies<? extends ProcessingUnitDependency> detailedDependencies) {
        dependencies.addDetailedDependencies(detailedDependencies);
        return this;
    }

    private String getDefaultZone() {
        String zone = this.name;
        if (zone == null) {
            //replace whitespaces
            zone = processingUnit;
            
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
    
    protected ProcessingUnitDeployment toProcessingUnitDeployment() {
        return config.toProcessingUnitDeployment();
    }

    protected Map<String,String> getElasticProperties() {
        return config.getElasticProperties();
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
}
