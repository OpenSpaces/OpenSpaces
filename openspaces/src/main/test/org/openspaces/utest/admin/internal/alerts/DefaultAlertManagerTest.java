package org.openspaces.utest.admin.internal.alerts;


import org.openspaces.admin.alert.AlertConfigurationException;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;
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
    
    //setConfig - An exception is raised if the alert is already enabled.
    //removeConfig - An exception is raised if the alert is already enabled.
    public void test_exception_is_raised_if_alert_already_enabled() {
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        alertManager.enableAlert(MockAlertConfiguration.class);
        
        //test
        MockAlertConfiguration test = new MockAlertConfiguration();
        test.setHighThreshold(91);
        test.setLowThreshold(81);
        try {
            alertManager.setConfig(test);
            fail();
        } catch(AlertConfigurationException e) {
            assertTrue( (e.getCause() instanceof EnabledBeanConfigCannotBeChangedException));
        }
        
        // test
        try {
            alertManager.removeConfig(MockAlertConfiguration.class);
            fail();
        } catch (AlertConfigurationException e) {
            assertTrue((e.getCause() instanceof EnabledBeanConfigCannotBeChangedException));
        }
    }
    
    //enableAlert - If the alert is already enabled, the request is silently ignored
    public void test_if_alert_is_already_enabled_request_ignored() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //setup
        MockAlertConfiguration mock = new MockAlertConfiguration();
        mock.setHighThreshold(90);
        mock.setLowThreshold(80);
        alertManager.setConfig(mock);
        alertManager.enableAlert(MockAlertConfiguration.class);
        
        //test
        alertManager.enableAlert(MockAlertConfiguration.class);
        MockAlertConfiguration test = alertManager.getConfig(MockAlertConfiguration.class);
        assertTrue(test.isEnabled());
        assertEquals(90, test.getHighThreshold().intValue());
        assertEquals(80, test.getLowThreshold().intValue());
    }
    
    //enableAlert - if the configuration was not previously set.
    //disableAlert - if the configuration was not previously set.
    //getConfig - if the configuration was not previously set.
    public void test_if_configuration_was_not_previously_set() {
        
        AlertManager alertManager = new DefaultAlertManager(new NullMockAdmin());
        
        //test
        try {
            alertManager.enableAlert(MockAlertConfiguration.class);
            fail();
        } catch (AlertConfigurationException e) {
            assertTrue(e.getCause() instanceof BeanConfigNotFoundException);
        }
        
        //test
        try {
            alertManager.disableAlert(MockAlertConfiguration.class);
            fail();
        } catch (AlertConfigurationException e) {
            assertTrue(e.getCause() instanceof BeanConfigNotFoundException);
        }
        
        //test
        try {
            alertManager.getConfig(MockAlertConfiguration.class);
            fail();
        } catch (AlertConfigurationException e) {
            assertTrue(e.getCause() instanceof BeanConfigNotFoundException);
        }
    }

}
