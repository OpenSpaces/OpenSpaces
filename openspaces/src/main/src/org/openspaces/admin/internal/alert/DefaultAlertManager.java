/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.alert;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertConfigurationException;
import org.openspaces.admin.alert.config.AlertConfiguration;
import org.openspaces.admin.alert.events.AlertTriggeredEventManager;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.internal.alert.bean.AlertBean;
import org.openspaces.admin.internal.alert.events.DefaultAlertEventManager;
import org.openspaces.admin.internal.alert.events.InternalAlertTriggeredEventManager;
import org.openspaces.core.bean.DefaultBeanServer;

public class DefaultAlertManager implements InternalAlertManager {

    private final Admin admin;
    private final InternalAlertTriggeredEventManager alertEventManager;
    private final AlertRepository alertRepository;
    private final BeanConfigPropertiesManager beanConfigPropertiesManager;

    public DefaultAlertManager(Admin admin) {
        this.admin = admin;
        this.alertEventManager = new DefaultAlertEventManager(this);
        this.alertRepository = new DefaultAlertRepository();
        this.beanConfigPropertiesManager = new DefaultBeanServer<AlertBean>(admin);
    }

    @Override
    public AlertRepository getAlertRepository() {
        return alertRepository;
    }
    
    @Override
    public AlertTriggeredEventManager getAlertTriggered() {
        return alertEventManager;
    }

    @Override
    public void triggerAlert(Alert alert) {
        alertRepository.addAlert(alert);
        alertEventManager.alertTriggered(alert);
    }

    @Override
    public Admin getAdmin() {
        return admin;
    }

    @Override
    public BeanConfigPropertiesManager getBeanConfigPropertiesManager() {
        return beanConfigPropertiesManager;
    }

    @Override
    public void disableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            beanConfigPropertiesManager.disableBean(configInstance.getBeanClassName());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to disable alert ["+clazz.getName()+"]", e);
        }
    }

    @Override
    public void enableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            beanConfigPropertiesManager.enableBean(configInstance.getBeanClassName());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to enable alert ["+clazz.getName()+"]", e);
        }
    }

    @Override
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

    @Override
    public boolean removeConfig(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            return beanConfigPropertiesManager.removeBeanConfig(configInstance.getBeanClassName());
        } catch (Exception e) {
            throw new AlertConfigurationException("Failed to remove alert configuration [" + clazz.getName() + "]", e);
        }
    }

    @Override
    public void setConfig(AlertConfiguration config) throws AlertConfigurationException {
        try {
            beanConfigPropertiesManager.setBeanConfig(config.getBeanClassName(), config.getProperties());
        }catch(Exception e) {
            throw new AlertConfigurationException("Failed to set alert configuration [" + config.getClass().getName() + "]", e);
        }
    }

    @Override
    public boolean isAlertEnabled(Class<? extends AlertConfiguration> clazz) {
        try {
            BeanConfig configInstance = getConfigInstance(clazz);
            return beanConfigPropertiesManager.isBeanEnabled(configInstance.getBeanClassName());
        }catch(Exception e) {
            return false; //ignore
        }
    }
    
    @Override
    public void configure(AlertConfiguration... configurations) throws AlertConfigurationException {
        for (AlertConfiguration configuration : configurations) {
            try {
                disableAlert(configuration.getClass());
            }catch(AlertConfigurationException e) {
                //silently ignore if called to enable/disable a bean with no configuration
                if (!(e.getCause() instanceof BeanConfigNotFoundException)) {
                    throw e;
                }
            }
            setConfig(configuration);
            if (configuration.isEnabled()) {
                enableAlert(configuration.getClass());
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
