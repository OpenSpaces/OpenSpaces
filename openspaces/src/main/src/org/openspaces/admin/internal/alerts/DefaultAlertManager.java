package org.openspaces.admin.internal.alerts;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alerts.Alert;
import org.openspaces.admin.alerts.events.AlertEventListener;
import org.openspaces.admin.alerts.strategy.AlertStrategyConfig;
import org.openspaces.admin.internal.alerts.events.DefaultAlertEventManager;
import org.openspaces.admin.internal.alerts.events.InternalAlertEventManager;
import org.openspaces.admin.internal.alerts.strategy.AlertStrategyBean;
import org.openspaces.admin.internal.strategy.DefaultStrategyPropertiesManager;
import org.openspaces.admin.strategy.StrategyAlreadyExistsException;
import org.openspaces.admin.strategy.StrategyConfig;
import org.openspaces.admin.strategy.StrategyNotFoundException;
import org.openspaces.admin.strategy.StrategyPropertiesManager;

public class DefaultAlertManager implements InternalAlertManager {

    private final Admin admin;
    private final InternalAlertEventManager alertEventManager;
    private final InternalAlertRepository alertRepository;
    private final StrategyPropertiesManager strategies;

    public DefaultAlertManager(Admin admin) {
        this.admin = admin;
        this.alertEventManager = new DefaultAlertEventManager(this);
        this.alertRepository = new DefaultAlertRepository();
        this.strategies = new DefaultStrategyPropertiesManager<AlertStrategyBean>(admin);
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

    public StrategyPropertiesManager getStrategies() {
        return strategies;
    }

    public void addStrategy(AlertStrategyConfig config) throws StrategyAlreadyExistsException {
        strategies.addStrategy(config.getStartegyBeanClassName(), config.getProperties());
    }

    public <T extends AlertStrategyConfig> void disableStrategy(Class<T> clazz) throws StrategyNotFoundException {
        StrategyConfig configInstance = getConfigInstance(clazz);
        strategies.disableStrategy(configInstance.getStartegyBeanClassName());
    }

    public <T extends AlertStrategyConfig> void enableStrategy(Class<T> clazz) throws StrategyNotFoundException {
        StrategyConfig configInstance = getConfigInstance(clazz);
        strategies.enableStrategy(configInstance.getStartegyBeanClassName());
    }

    public <T extends AlertStrategyConfig> T getStrategy(Class<T> clazz) throws StrategyNotFoundException {
        T configInstance = getConfigInstance(clazz);
        Map<String, String> strategyProperties = strategies.getStrategy(configInstance.getStartegyBeanClassName());
        configInstance.setProperties(strategyProperties);
        return configInstance;
    }

    public <T extends AlertStrategyConfig> void removeStrategy(Class<T> clazz) throws StrategyNotFoundException {
        StrategyConfig configInstance = getConfigInstance(clazz);
        strategies.removeStrategy(configInstance.getStartegyBeanClassName());
    }

    public void setStrategy(AlertStrategyConfig config) throws StrategyNotFoundException {
        strategies.setStrategy(config.getStartegyBeanClassName(), config.getProperties());
    }

    private <T extends StrategyConfig> T getConfigInstance(Class<T> clazz) {
        try {
            T newInstance = clazz.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new StrategyNotFoundException("Unable to extract strategy name from " + clazz, e);
        }
    }
}
