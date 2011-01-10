package org.openspaces.admin.internal.alert;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertConfigurationException;
import org.openspaces.admin.alert.config.AlertConfiguration;
import org.openspaces.admin.alert.events.AlertEventManager;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.internal.alert.bean.AlertBean;
import org.openspaces.admin.internal.alert.events.DefaultAlertEventManager;
import org.openspaces.admin.internal.alert.events.InternalAlertEventManager;
import org.openspaces.core.bean.DefaultBeanServer;

public class DefaultAlertManager implements InternalAlertManager {

    private final Admin admin;
    private final InternalAlertEventManager alertEventManager;
    private final InternalAlertRepository alertRepository;
    private final BeanConfigPropertiesManager beanConfigPropertiesManager;

    public DefaultAlertManager(Admin admin) {
        this.admin = admin;
        this.alertEventManager = new DefaultAlertEventManager(this);
        this.alertRepository = new DefaultAlertRepository();
        this.beanConfigPropertiesManager = new DefaultBeanServer<AlertBean>(admin);
    }

    public AlertRepository getAlertRepository() {
        return alertRepository;
    }
    
    public AlertEventManager getAlertEventManager() {
        return alertEventManager;
    }

    public void fireAlert(Alert alert) {
        boolean added = alertRepository.addAlert(alert);
        if (added) {
            alertEventManager.onAlert(alert);
        }
    }

    public Admin getAdmin() {
        return admin;
    }

    public BeanConfigPropertiesManager getBeanConfigPropertiesManager() {
        return beanConfigPropertiesManager;
    }

    public void disableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            beanConfigPropertiesManager.disableBean(configInstance.getBeanClassName());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to disable alert ["+clazz.getName()+"]", e);
        }
    }

    public void enableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            beanConfigPropertiesManager.enableBean(configInstance.getBeanClassName());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to enable alert ["+clazz.getName()+"]", e);
        }
    }

    public <T extends AlertConfiguration> T getConfig(Class<T> clazz) throws AlertConfigurationException {
        try {
            T configInstance = getConfigInstance(clazz);
            Map<String, String> beanProperties = beanConfigPropertiesManager.getBeanConfig(configInstance.getBeanClassName());
            configInstance.setProperties(beanProperties);
            configInstance.setEnabled(beanConfigPropertiesManager.isBeanEnabled(configInstance.getBeanClassName()));
            return configInstance;
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to get alert configuration ["+clazz.getName()+"]", e);
        }
    }

    public boolean removeConfig(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            return beanConfigPropertiesManager.removeBeanConfig(configInstance.getBeanClassName());
        } catch (Exception e) {
            throw new AlertConfigurationException("Failed to remove alert configuration [" + clazz.getName() + "]", e);
        }
    }

    public void setConfig(AlertConfiguration config) throws AlertConfigurationException {
        try {
            beanConfigPropertiesManager.setBeanConfig(config.getBeanClassName(), config.getProperties());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to set alert configuration [" + config.getClass().getName() + "]", e);
        }
    }

    public boolean isAlertEnabled(Class<? extends AlertConfiguration> clazz) {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            return beanConfigPropertiesManager.isBeanEnabled(configInstance.getBeanClassName());
        }catch(Exception e) {
            return false; //ignore
        }
    }
    
    public void configure(AlertConfiguration[] configurations) throws AlertConfigurationException {
        for (AlertConfiguration configuration : configurations) {
            setConfig(configuration);
            if (configuration.isEnabled()) {
                enableAlert(configuration.getClass());
            } else {
                disableAlert(configuration.getClass());
            }
        }
    }
    
    private <T extends BeanConfig> T getConfigInstance(Class<T> clazz) {
        try {
            T newInstance = clazz.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new BeanConfigNotFoundException("Unable to extract bean name from " + clazz, e);
        }
    }
}
