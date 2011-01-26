package org.openspaces.utest.admin.internal.alerts;


import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.internal.alert.DefaultAlertManager;
import org.openspaces.utest.admin.internal.admin.NullMockAdmin;

import junit.framework.TestCase;

public class DefaultAlertManagerTest extends TestCase {
    
    public void test_reConfigure_a_predefined_alert() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        alertManager.enableAlert(MockAlertConfiguration.class);
        
        //reconfigure
        MockAlertConfiguration config = alertManager.getConfig(MockAlertConfiguration.class);
        config.setHighThreshold(85);
        alertManager.configure(config);
        
        //test
        MockAlertConfiguration test = alertManager.getConfig(MockAlertConfiguration.class);
        assertTrue(test.isEnabled());
        assertEquals(85, test.getHighThreshold().intValue());
        assertEquals(80, test.getLowThreshold().intValue());
    }
    
    public void test_enable_a_predefined_disabled_alert() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        assertFalse(alertManager.getConfig(MockAlertConfiguration.class).isEnabled());
        
        //enable
        alertManager.enableAlert(MockAlertConfiguration.class);
        
        //test
        assertTrue(alertManager.getConfig(MockAlertConfiguration.class).isEnabled());
        
    }
    
    public void test_configure_and_enable_a_predefined_disabled_alert() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        assertFalse(alertManager.getConfig(MockAlertConfiguration.class).isEnabled());
        
        //configure and enable
        MockAlertConfiguration config = alertManager.getConfig(MockAlertConfiguration.class);
        config.setHighThreshold(85);
        config.setEnabled(true); //don't forget
        alertManager.configure(config);
        
        //test
        MockAlertConfiguration test = alertManager.getConfig(MockAlertConfiguration.class);
        assertTrue(test.isEnabled());
        assertEquals(85, test.getHighThreshold().intValue());
        assertEquals(80, test.getLowThreshold().intValue());
    }
    
    public void test_configure_an_undefined_alert() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        mock.setEnabled(true);
        alertManager.configure(mock);
        
        //test
        MockAlertConfiguration test = alertManager.getConfig(MockAlertConfiguration.class);
        assertTrue(test.isEnabled());
        assertEquals(90, test.getHighThreshold().intValue());
        assertEquals(80, test.getLowThreshold().intValue());
    }
    
    public void test_disable_a_predefined_enabled_alert() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        alertManager.enableAlert(MockAlertConfiguration.class);
        
        //disable
        alertManager.disableAlert(MockAlertConfiguration.class);
        
        //test
        MockAlertConfiguration test = alertManager.getConfig(MockAlertConfiguration.class);
        assertFalse(test.isEnabled());
        assertEquals(90, test.getHighThreshold().intValue());
        assertEquals(80, test.getLowThreshold().intValue());
    }
}
