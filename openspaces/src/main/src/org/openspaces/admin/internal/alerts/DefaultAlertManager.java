package org.openspaces.admin.internal.alerts;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.config.AlertBeanConfig;
import org.openspaces.admin.alerts.events.AlertEventListener;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.internal.alerts.bean.AlertBean;
import org.openspaces.admin.internal.alerts.events.DefaultAlertEventManager;
import org.openspaces.admin.internal.alerts.events.InternalAlertEventManager;
import org.openspaces.admin.bean.BeanConfigAlreadyExistsException;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
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

    public void add(AlertEventListener listener) {
        alertEventManager.add(listener);
    }

    public void add(AlertEventListener listener, boolean includeExisting) {
        alertEventManager.add(listener, includeExisting);
    }

    public void remove(AlertEventListener listener) {
        alertEventManager.remove(listener);
    }

    public void fireAlert(Alert alert) {
        alertRepository.addAlert(alert);
        alertEventManager.onAlert(alert);
    }

    public Admin getAdmin() {
        return admin;
    }

    public BeanConfigPropertiesManager getBeanConfigPropertiesManager() {
        return beanConfigPropertiesManager;
    }

    public void addConfig(AlertBeanConfig config) throws BeanConfigAlreadyExistsException {
        beanConfigPropertiesManager.addConfig(config.getBeanClassName(), config.getProperties());
    }

    public <T extends AlertBeanConfig> void disableConfig(Class<T> clazz) throws BeanConfigNotFoundException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanConfigPropertiesManager.disableConfig(configInstance.getBeanClassName());
    }

    public <T extends AlertBeanConfig> void enableConfig(Class<T> clazz) throws BeanConfigNotFoundException, BeanConfigException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanConfigPropertiesManager.enableConfig(configInstance.getBeanClassName());
    }

    public <T extends AlertBeanConfig> T getConfig(Class<T> clazz) throws BeanConfigNotFoundException {
        T configInstance = getConfigInstance(clazz);
        Map<String, String> beanProperties = beanConfigPropertiesManager.getConfig(configInstance.getBeanClassName());
        configInstance.setProperties(beanProperties);
        return configInstance;
    }

    public <T extends AlertBeanConfig> void removeConfig(Class<T> clazz) throws BeanConfigNotFoundException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanConfigPropertiesManager.removeConfig(configInstance.getBeanClassName());
    }

    public void setConfig(AlertBeanConfig config) throws BeanConfigNotFoundException {
        beanConfigPropertiesManager.setConfig(config.getBeanClassName(), config.getProperties());
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
