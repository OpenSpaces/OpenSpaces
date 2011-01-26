package org.openspaces.utest.admin.internal.alerts;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.alert.bean.AlertBean;

public class MockAlertBean implements AlertBean {

    public static final String beanUID = "d0ae9aaec204";
    public static final String ALERT_NAME = "Mock Alert";
    
    private final MockAlertConfiguration config = new MockAlertConfiguration();

    private Admin admin;

    public MockAlertBean() {
    }

    public void afterPropertiesSet() throws Exception {
    }

    public void destroy() throws Exception {
    }

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }
}
