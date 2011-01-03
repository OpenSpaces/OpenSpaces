package org.openspaces.admin.internal.pu.elastic;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.core.util.StringProperties;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;

public abstract class AbstractElasticProcessingUnitDeployment {
    
//    private static final String RESERVED_ISOLATION_CONTEXT_PROPERTY = "isolation";
//    public static final String RESERVED_ZONE_CONTEXT_PROPERTY = "zone";
//    public static final String RESERVED_TENANT_CONTEXT_PROPERTY = "tenant";
//    private static final String TENANT_ZONE_SEPERATOR_DEFAULT = "__";
//    private static final String RESERVED_CONTEXT_PROPERTY_PREFIX_DEFAULT ="__";
//    public static final String RESERVED_CONTEXT_PROPERTY_PREFIX_PROPERTY="reserved-context-property-prefix";
//    private static final String TENANT_ZONE_SEPERATOR_PROPERTY="tenant-zone-separator";

//  private String agentZone;
//  private ElasticProcessingUnitDeploymentIsolation isolation = new DedicatedIsolation();
    
    private final String processingUnit;
    private String name;
    private final StringProperties contextProperties = new StringProperties();
    private UserDetails userDetails;
    private boolean secured;
    private final Map<String,String> elasticProperties;

    private GridServiceContainerConfig containerConfig;
    private MachineProvisioningBeanPropertiesManager machineProvisioningPropertiesManager;
    private ScaleStrategyBeanPropertiesManager scaleStrategyPropertiesManager;
    
    
//    private String tenantZoneSeparator = TENANT_ZONE_SEPERATOR_DEFAULT;
//    private String reservedContextPropertyPrefix = RESERVED_CONTEXT_PROPERTY_PREFIX_DEFAULT;
        

    public AbstractElasticProcessingUnitDeployment(String processingUnit) {
        this.processingUnit = processingUnit;
        elasticProperties = new HashMap<String,String>();
        containerConfig = new GridServiceContainerConfig(elasticProperties);
        machineProvisioningPropertiesManager = new MachineProvisioningBeanPropertiesManager(elasticProperties);
        scaleStrategyPropertiesManager = new ScaleStrategyBeanPropertiesManager(elasticProperties);
    }

    /**
     * Reserved context properties start with the {@link AbstractElasticProcessingUnitDeployment#RESERVED_CONTEXT_PROPERTY_PREFIX_DEFAULT} prefix.
     * This method is a hook for overriding the default reserved context property prefix.
     */
//    protected void setReservedContextPropertyPrefix(String prefix) {
//        this.reservedContextPropertyPrefix = prefix;
//    }
    
    /**
     * Tenant and Zone strings are concatenated with {@link AbstractElasticProcessingUnitDeployment#TENANT_ZONE_SEPERATOR_DEFAULT} to form a unique per tenant zone string.
     * This method is a hook for overriding the default concatenation separator string.
     */
//    protected void setTenantZoneSeperator(String separator) {
//        this.tenantZoneSeparator = separator;
//    }
        
    protected AbstractElasticProcessingUnitDeployment setContextProperty(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        String currentValue = contextProperties.get(key,null);
        if (currentValue != null && !currentValue.equals(value)) {
            throw new IllegalStateException("Context property " + key + " is already defined to " + currentValue + " and cannot be modified to " + value);
        }
        contextProperties.put(key, value);
        return this;
    }

    protected AbstractElasticProcessingUnitDeployment secured(boolean secured) {
        this.secured = secured;
        return this;
    }


/* NOT IMPLEMENTED
    protected AbstractElasticProcessingUnitDeployment zone(String zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone cannot be null");
        }
        if (this.agentZone != null && !this.agentZone.equals(zone)) {
            throw new IllegalStateException("Zone is already defined to " + this.agentZone + " and cannot be modified to " + zone);
        }
        this.agentZone = zone;
        return this;
    }
*/

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
    protected AbstractElasticProcessingUnitDeployment useScript() {
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
    
/* NOT IMPLEMENTED
    protected AbstractElasticProcessingUnitDeployment isolation(ElasticProcessingUnitDeploymentIsolation isolation) {
        this.isolation = isolation;
        return this;
    }
*/
    protected AbstractElasticProcessingUnitDeployment machineProvisioning(BeanConfig config) {
        enableBean(machineProvisioningPropertiesManager, config);
        return this;
    }    

    protected AbstractElasticProcessingUnitDeployment scale(BeanConfig config) {
        enableBean(scaleStrategyPropertiesManager, config);
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
        
        ProcessingUnitDeployment deployment = 
            new ProcessingUnitDeployment(this.processingUnit)
            .maxInstancesPerVM(1);
        
        if (this.name != null) {
            deployment.name(name);
        }
        
        if (this.secured) {
            deployment.secured(secured);
        }
        
        if (this.userDetails != null) {
            deployment.userDetails(userDetails);
        }

// NOT IMPLEMENTED        
//        deployment.setContextProperty(RESERVED_CONTEXT_PROPERTY_PREFIX_PROPERTY, reservedContextPropertyPrefix);
//        deployment.setContextProperty(TENANT_ZONE_SEPERATOR_PROPERTY, tenantZoneSeparator);      
//        deployment.setContextProperty(reservedContextPropertyPrefix + RESERVED_ISOLATION_CONTEXT_PROPERTY, this.isolation.getIsolationType());

//        String tenant = null;
//        if (this.isolation instanceof SharedTenantIsolation) {
//            tenant = ((SharedTenantIsolation)isolation).getTenant();
//        }
        
        String containerZone = getDefaultZone();
            
//        deployment.setContextProperty(this.reservedContextPropertyPrefix + RESERVED_ZONE_CONTEXT_PROPERTY, containerZone);
        
//        if (this.isolation instanceof SharedTenantIsolation) {
//            
//            deployment.setContextProperty(this.reservedContextPropertyPrefix + RESERVED_TENANT_CONTEXT_PROPERTY, tenant);
//        
//            // Protect against spill over of tenant and zone strings by rejecting strings that contain the separator
//            // Advanced users can override the separator with the {@link AbstractElasticProcessingUnitDeployment#setTenantZoneSeperator(String)} protected method.
//        
//            if (agentZone.contains(tenantZoneSeparator)) {
//                throw new IllegalStateException("Zone must not container the seperator '"+tenantZoneSeparator + "' string");
//            }
//        
//            if (tenant.contains(tenantZoneSeparator)) {
//                throw new IllegalStateException("Tenant must not container the seperator '"+tenantZoneSeparator + "' string");
//            }
//        
//            containerZone += TENANT_ZONE_SEPERATOR_DEFAULT + tenant;
//        }
//        
        deployment.addZone(containerZone);
        commandLineArgument("-Dcom.gs.zones=" + containerZone);
    
                
        Map<String,String> context = contextProperties.getProperties();
        for (String key : context.keySet()) {
        
            // Protect against overriding reserved context properties
            // Advanced users can override the separator with the {@link AbstractElasticProcessingUnitDeployment#setReservedContextPropertyPrefix()} protected method.
//            if (key.startsWith(reservedContextPropertyPrefix)) {
//                throw new IllegalStateException("Context property must not start with the reserved '"+reservedContextPropertyPrefix+ "' prefix.");
//            }
            
            String value = context.get(key);
            deployment.setContextProperty(key, value);
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
    private void enableBean(BeanConfigPropertiesManager propertiesManager, BeanConfig config) {
        propertiesManager.disableAllBeans();
        propertiesManager.setBeanConfig(config.getBeanClassName(), config.getProperties());
        propertiesManager.enableBean(config.getBeanClassName());
    }
    
}
