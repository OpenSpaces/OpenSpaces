package org.openspaces.admin.internal.strategy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.strategy.StrategyAlreadyExistsException;
import org.openspaces.admin.strategy.StrategyNotFoundException;
import org.openspaces.admin.strategy.StrategyPropertiesManager;

public class DefaultStrategyPropertiesManager<T extends StrategyBean> implements StrategyPropertiesManager {

    private final Map<String, Map<String, String>> strategyProperties = new HashMap<String, Map<String, String>>();
    private final Map<String, T> enabledStrategies = new HashMap<String, T>();
    private final Admin admin;

    public DefaultStrategyPropertiesManager(Admin admin) {
        this.admin = admin;
    }

    public void addStrategy(String strategy, Map<String, String> properties) throws StrategyAlreadyExistsException {
        if (strategyProperties.containsKey(strategy)) {
            throw new StrategyAlreadyExistsException("Failed to add startegy [" + strategy + "] - already exists.");
        }
        strategyProperties.put(strategy, properties);
    }

    public void setStrategy(String strategy, Map<String, String> properties) throws StrategyNotFoundException {

        if (!strategyProperties.containsKey(strategy)) {
            throw new StrategyNotFoundException("Failed to set startegy [" + strategy + "] - doesn't exist.");
        }

        if (enabledStrategies.containsKey(strategy)) {
            disableStrategy(strategy);
            strategyProperties.put(strategy, properties);
            enableStrategy(strategy);
        } else {
            strategyProperties.put(strategy, properties);
        }
    }

    public void enableStrategy(String strategy) throws StrategyNotFoundException {
        if (!strategyProperties.containsKey(strategy)) {
            throw new StrategyNotFoundException("Failed to enable startegy [" + strategy + "] - doesn't exist.");
        }

        if (enabledStrategies.containsKey(strategy)) {
            return; // idempotent - already enabled just return
        }

        T strategyBean = newInstance(strategy);
        enabledStrategies.put(strategy, strategyBean);

        // TODO - due to sharing of statistics monitor, need to have reference counter for
        // start/stop monitor calls.
        // until we do, we will create a different admin object for each strategy.
        strategyBean.setAdmin(admin);
        strategyBean.setProperties(strategyProperties.get(strategy));
        strategyBean.afterPropertiesSet();
    }

    public void disableStrategy(String strategy) throws StrategyNotFoundException {
        if (!strategyProperties.containsKey(strategy)) {
            throw new StrategyNotFoundException("Failed to disable startegy [" + strategy + "] - doesn't exist.");
        }
        if (enabledStrategies.containsKey(strategy)) {
            T strategyBean = enabledStrategies.remove(strategy);
            strategyBean.destroy();
        }
    }

    public void removeStrategy(String strategy) throws StrategyNotFoundException {
        if (!strategyProperties.containsKey(strategy)) {
            throw new StrategyNotFoundException("Failed to remove startegy [" + strategy + "] - doesn't exist.");
        }
        disableStrategy(strategy);
        strategyProperties.remove(strategy);
    }

    public Map<String, String> getStrategy(String strategy) throws StrategyNotFoundException {
        if (!strategyProperties.containsKey(strategy)) {
            throw new StrategyNotFoundException("Failed to get startegy [" + strategy + "] - doesn't exist.");
        }

        // clone to simulate serialization - client code can't influence contents of map directly.
        Map<String, String> properties = new HashMap<String, String>(strategyProperties.get(strategy));
        return properties;
    }

    public String[] getStrategies() {
        String[] startegies = strategyProperties.keySet().toArray(new String[strategyProperties.size()]);
        return startegies;
    }

    public String[] getEnabledStrategies() {
        String[] startegies = enabledStrategies.keySet().toArray(new String[enabledStrategies.size()]);
        return startegies;
    }

    public void disableAllStrategies() {
        for (Iterator<T> iter = enabledStrategies.values().iterator(); iter.hasNext();) {
            T strategyBean = iter.next();
            strategyBean.destroy();
            iter.remove();
        }
    }

    @SuppressWarnings("unchecked") 
    private T newInstance(String strategyBeanClassName) throws StrategyNotFoundException {
        try {
            Class<T> clazz = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(
                    strategyBeanClassName);
            T newInstance = clazz.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new StrategyNotFoundException("Failed to instantiate strategy bean class [" + strategyBeanClassName
                    + "]", e);
        }
    }
}
