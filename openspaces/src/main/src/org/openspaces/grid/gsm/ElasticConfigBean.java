package org.openspaces.grid.gsm;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.core.bean.Bean;
import org.openspaces.grid.gsm.machines.DefaultMachineProvisioning;

public class ElasticConfigBean implements Bean {

    Map<String,String> properties;
    
    GridServiceContainerConfig getGridServiceContainerConfig() {
        return new GridServiceContainerConfig(properties);
    }


    public DiscoveredMachineProvisioningConfig getDiscoveredMachineProvisioningConfig() {
        
        final MachineProvisioningBeanPropertiesManager propertiesManager = 
            new MachineProvisioningBeanPropertiesManager(properties);
        
        propertiesManager.getBeanConfig(DefaultMachineProvisioning.class.getName());
        
        return new DiscoveredMachineProvisioningConfig(properties);
    }
    
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void destroy() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setAdmin(Admin admin) {
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
