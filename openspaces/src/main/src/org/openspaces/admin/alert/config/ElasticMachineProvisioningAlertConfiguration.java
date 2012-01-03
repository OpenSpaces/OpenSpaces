package org.openspaces.admin.alert.config;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.alert.bean.ElasticMachineProvisioningAlertBean;

public class ElasticMachineProvisioningAlertConfiguration implements AlertConfiguration {

    private static final long serialVersionUID = 1L;

    
    private final Map<String,String> properties = new HashMap<String, String>();

    private boolean enabled;

    /**
     * Constructs an empty machine CPU utilization alert configuration.
     */
    public ElasticMachineProvisioningAlertConfiguration() {
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanClassName() {
        return ElasticMachineProvisioningAlertBean.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
