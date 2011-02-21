package org.openspaces.admin.internal.pu.elastic;

import java.util.Map;

public class MachineProvisioningBeanPropertiesManager 
    extends FlattenedBeanConfigPropertiesManager {
        
    private static final String ELASTIC_MACHINE_PROVISIONING_CLASSNAMES_KEY = "elastic-machine-provisioning-classnames";
    private static final String ELASTIC_MACHINE_PROVISIONING_ENABLED_CLASSNAME_KEY = "elastic-machine-provisioning-enabled-classname";
    
    public MachineProvisioningBeanPropertiesManager(
            Map<String, String> properties) {
        super(ELASTIC_MACHINE_PROVISIONING_CLASSNAMES_KEY, 
              ELASTIC_MACHINE_PROVISIONING_ENABLED_CLASSNAME_KEY, 
              properties);
    }
}
