/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openspaces.admin.alerts;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.alerts.config.AlertBeanConfig;
import org.openspaces.admin.alerts.events.AlertEventListener;
import org.openspaces.admin.alerts.events.AlertEventManager;
import org.openspaces.admin.bean.BeanConfigManager;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;

/**
 * An alert manager is a manager for alerts issued by alert beans or alert providers.
 * <p>
 * The alert manager provides two options for configuration of alert beans: A strongly typed
 * configuration based on an implementation of {@link AlertBeanConfig}, or a weakly typed
 * configuration based on String key-value property pairs (see {@link BeanConfigPropertiesManager}).
 * <p>
 * To Register/Unregister for alert events (of all types), add/remove {@link AlertEventListener}s
 * exposed by the {@link AlertEventManager} API.
 * <p>
 * The {@link #fireAlert(Alert)} method call allows alert beans to 'fire' an alert and trigger an
 * event to be sent to all registered alert event listeners.
 * <p>
 * An alert provider is a remote service implementing an {@link AlertProvider} interface. A
 * dedicated alert bean needs to be configured to periodically perform remote calls to poll issued
 * alerts (see {@link AlertProvider#getAlerts()}), and 'fire' them to local alert event listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertManager extends BeanConfigManager<AlertBeanConfig>, AlertEventManager, AdminAware  {
	
    /**
     * @return the weakly typed configuration API. 
     */
	BeanConfigPropertiesManager getBeanConfigPropertiesManager();
	
	/**
	 * Trigger an alert event for registered alert event listeners.
	 * @param alert an alert.
	 */
	void fireAlert(Alert alert);
}
