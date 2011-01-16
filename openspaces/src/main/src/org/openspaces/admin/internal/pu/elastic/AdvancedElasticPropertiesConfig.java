package org.openspaces.admin.internal.pu.elastic;

import java.util.Map;

import org.openspaces.core.util.StringProperties;

public class AdvancedElasticPropertiesConfig {

    private static final String ALLOW_DEPLOYMENT_ON_MANAGEMENT_MACHINE_KEY = "container.allow-deployment-on-management-machine";
    private static final boolean ALLOW_DEPLOYMENT_ON_MANAGEMENT_MACHINE_DEFAULT = true;

    StringProperties properties;
    
    public AdvancedElasticPropertiesConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }
    
    public boolean getAllowDeploymentOnManagementMachine() {
        return properties.getBoolean(ALLOW_DEPLOYMENT_ON_MANAGEMENT_MACHINE_KEY, ALLOW_DEPLOYMENT_ON_MANAGEMENT_MACHINE_DEFAULT);
    }

    /**
     * Allows the {@link org.openspaces.admin.gsc.GridServiceContainer} to be started on the same machine as the LUS/GSM/ESM
     */
    public void setAllowDeploymentOnManagementMachine(boolean allowDeploymentOnManagementMachine) {
        properties.putBoolean(ALLOW_DEPLOYMENT_ON_MANAGEMENT_MACHINE_KEY, allowDeploymentOnManagementMachine);
    }
}
