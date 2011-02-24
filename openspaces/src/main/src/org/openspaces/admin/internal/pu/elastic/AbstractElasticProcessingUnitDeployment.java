package org.openspaces.admin.internal.pu.elastic;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.core.util.MemoryUnit;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

public abstract class AbstractElasticProcessingUnitDeployment {

    private final String processingUnit;
    private String name;
    private final StringProperties contextProperties = new StringProperties();
    private final StringProperties defaultContextProperties = new StringProperties();
    private UserDetails userDetails;
    private boolean secured;
    private final Map<String,String> elasticProperties;

    private final GridServiceContainerConfig containerConfig;
    private final ElasticMachineIsolationConfig isolationConfig;
    private final MachineProvisioningBeanPropertiesManager machineProvisioningPropertiesManager;
    private final ScaleStrategyBeanPropertiesManager scaleStrategyPropertiesManager;
    private ElasticMachineProvisioningConfig machineProvisioning;
    private ScaleStrategyConfig scaleStrategy;
    
    public AbstractElasticProcessingUnitDeployment(String processingUnit) {
        this.processingUnit = processingUnit;
        elasticProperties = new HashMap<String,String>();
        containerConfig = new GridServiceContainerConfig(elasticProperties);
        isolationConfig = new ElasticMachineIsolationConfig(elasticProperties);
        machineProvisioningPropertiesManager = new MachineProvisioningBeanPropertiesManager(elasticProperties);
        scaleStrategyPropertiesManager = new ScaleStrategyBeanPropertiesManager(elasticProperties);
    }
        
    protected void addContextPropertyDefault(String key, String defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        defaultContextProperties.put(key,defaultValue);
    }
    
    protected AbstractElasticProcessingUnitDeployment addContextProperty(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        contextProperties.put(key, value);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment secured(boolean secured) {
        this.secured = secured;
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment name(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (this.name != null && !this.name.equals(name)) {
            throw new IllegalStateException("Name is already defined to " + this.name + " and cannot be modified to " + name);
        }
        this.name = name;
        return this;
    }


    protected AbstractElasticProcessingUnitDeployment userDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User details cannot be null");
        }
        if (this.userDetails != null && !this.userDetails.equals(userDetails)) {
            throw new IllegalStateException("User details are already defined and cannot be modified.");
        }
        this.userDetails = userDetails;
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
        containerConfig.setUseScript(true);
        return this;
    }

    /**
     * Will cause JVM options added using {@link #commandLineArgument(String)} to override all the vm arguments
     * that the JVM will start by default with.
     */
    protected AbstractElasticProcessingUnitDeployment overrideCommandLineArguments() {
        containerConfig.setOverrideCommandLineArguments(true);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        commandLineArgument("-Xmx"+memoryCapacityPerContainer);
        commandLineArgument("-Xms"+memoryCapacityPerContainer);
        return this;
    }
    
    protected AbstractElasticProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        memoryCapacityPerContainer(unit.toMegaBytes(memoryCapacityPerContainer)+MemoryUnit.MEGABYTES.getPostfix());
        return this;
    }
    
    /**
     * Will add a JVM level argument when the process is executed using pure JVM. For example, the memory
     * can be controlled using <code>-Xmx512m</code>.
     */
    protected AbstractElasticProcessingUnitDeployment commandLineArgument(String argument) {
        containerConfig.addCommandLineArgument(argument);
        return this;
    }

    /**
     * Sets an environment variable that will be passed to forked process.
     */
    protected AbstractElasticProcessingUnitDeployment environmentVariable(String name, String value) {
        containerConfig.setEnvironmentVariable(name, value);
        return this;
    }
    
    protected AbstractElasticProcessingUnitDeployment machineProvisioning(ElasticMachineProvisioningConfig config, String sharingId) {
        isolationConfig.setSharingId(sharingId);
        if ((config.getGridServiceAgentZones() == null || config.getGridServiceAgentZones().length == 0) && config.isGridServiceAgentZoneMandatory()) {
            throw new IllegalArgumentException("isGridServiceAgentZoneMandatory returns true, but no Grid Service Agent zone is specified.");
        }
        machineProvisioning = config;
        return this;
    }    

    protected AbstractElasticProcessingUnitDeployment scale(ScaleStrategyConfig config) {
        scaleStrategy = config;
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

        enableBean(machineProvisioningPropertiesManager, machineProvisioning);
        enableBean(scaleStrategyPropertiesManager, scaleStrategy);
            
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
        return deployment;
    }

    protected Map<String,String> getElasticProperties() {
        return this.elasticProperties;
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
