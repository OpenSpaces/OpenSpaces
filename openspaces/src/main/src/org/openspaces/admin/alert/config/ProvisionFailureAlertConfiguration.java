package org.openspaces.admin.alert.config;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.alert.bean.ProvisionFailureAlertBean;

/**
 * A provision failure alert configuration. An alert is raised if the processing unit has less
 * actual instances than planned instances. An alert is resolved when the processing unit actual
 * instance count is equal to the planned instance count.
 * @see ProvisionFailureAlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0.2
 */
public class ProvisionFailureAlertConfiguration implements AlertConfiguration {
    private static final long serialVersionUID = 1L;
    
    private final Map<String,String> properties = new HashMap<String, String>();

    private boolean enabled;
    
    /**
     * Constructs an empty provision failure alert configuration.
     */
    public ProvisionFailureAlertConfiguration() {
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

    }

    /**
     * {@inheritDoc}
     */
    public String getBeanClassName() {
        return ProvisionFailureAlertBean.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

}
