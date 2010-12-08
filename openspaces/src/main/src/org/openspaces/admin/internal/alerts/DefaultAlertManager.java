package org.openspaces.admin.internal.alerts;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.config.AlertBeanConfig;
import org.openspaces.admin.alerts.events.AlertEventListener;
import org.openspaces.admin.bean.BeanPropertiesManager;
import org.openspaces.admin.internal.alerts.bean.AlertBean;
import org.openspaces.admin.internal.alerts.events.DefaultAlertEventManager;
import org.openspaces.admin.internal.alerts.events.InternalAlertEventManager;
import org.openspaces.admin.bean.BeanAlreadyExistsException;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.BeanNotFoundException;
import org.openspaces.core.bean.DefaultBeanServer;

public class DefaultAlertManager implements InternalAlertManager {

    private final Admin admin;
    private final InternalAlertEventManager alertEventManager;
    private final InternalAlertRepository alertRepository;
    private final BeanPropertiesManager beanPropertiesManager;

    public DefaultAlertManager(Admin admin) {
        this.admin = admin;
        this.alertEventManager = new DefaultAlertEventManager(this);
        this.alertRepository = new DefaultAlertRepository();
        this.beanPropertiesManager = new DefaultBeanServer<AlertBean>(admin);
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

    public BeanPropertiesManager getBeanPropertiesManager() {
        return beanPropertiesManager;
    }

    public void addBean(AlertBeanConfig config) throws BeanAlreadyExistsException {
        beanPropertiesManager.addBean(config.getBeanClassName(), config.getProperties());
    }

    public <T extends AlertBeanConfig> void disableBean(Class<T> clazz) throws BeanNotFoundException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanPropertiesManager.disableBean(configInstance.getBeanClassName());
    }

    public <T extends AlertBeanConfig> void enableBean(Class<T> clazz) throws BeanNotFoundException, BeanException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanPropertiesManager.enableBean(configInstance.getBeanClassName());
    }

    public <T extends AlertBeanConfig> T getBean(Class<T> clazz) throws BeanNotFoundException {
        T configInstance = getConfigInstance(clazz);
        Map<String, String> beanProperties = beanPropertiesManager.getBean(configInstance.getBeanClassName());
        configInstance.setProperties(beanProperties);
        return configInstance;
    }

    public <T extends AlertBeanConfig> void removeBean(Class<T> clazz) throws BeanNotFoundException {
        BeanConfig configInstance = getConfigInstance(clazz);
        beanPropertiesManager.removeBean(configInstance.getBeanClassName());
    }

    public void setBean(AlertBeanConfig config) throws BeanNotFoundException {
        beanPropertiesManager.setBean(config.getBeanClassName(), config.getProperties());
    }

    private <T extends BeanConfig> T getConfigInstance(Class<T> clazz) {
        try {
            T newInstance = clazz.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new BeanNotFoundException("Unable to extract bean name from " + clazz, e);
        }
    }
}
