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
package org.openspaces.admin.alert;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.alert.config.AlertConfiguration;
import org.openspaces.admin.alert.config.parser.AlertConfigurationParser;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.alert.events.AlertTriggeredEventManager;

/**
 * The <tt>AlertManager</tt> is a manager for configuring of alerts, firing of alerts, and
 * registering for alert events.
 * <p>
 * The alert manager provides two options for configuration of alert configurations - as bulk or one
 * by one. The {@link AlertConfiguration} is the main interface of an alert configuration - a
 * configuration based on String key-value property pairs.
 * <p>
 * To configure with a bulk of alert configurations, the {@link #configure(AlertConfiguration[])}
 * method can be used. These configurations can be extracted from a resource using an
 * {@link AlertConfigurationParser}. The parser {@link AlertConfigurationParser#parse()} returns an
 * array of {@link AlertConfiguration}. The {@link XmlAlertConfigurationParser} can be used to parse
 * the <tt>alerts.xml</tt> configuration file located under <tt>config/alerts</tt>.
 * <p>
 * To Register/Unregister for alert events (of all types), use the {@link AlertTriggeredEventManager} to
 * add/remove {@link AlertTriggeredEventListener}s.
 * <p>
 * The {@link #triggerAlert(Alert)} method call allows <tt>alert triggers</tt> to 'fire' an alert and
 * trigger an event to be sent to all registered alert event listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertManager extends AdminAware {

    /**
     * Define an alert configuration for a 'bulk' of configurations. Overrides all previously set
     * properties for any of the alerts.
     * <p>
     * See the {@link XmlAlertConfigurationParser} which can be used to parse the default
     * <tt>alerts.xml</tt> configuration file located under <tt>config/alerts</tt>.
     * <p>
     * If {@link AlertConfiguration#isEnabled()} returns <code>true</code> it is equivalent to
     * calling (for a predefined alert) {@link #disableAlert(Class)}, followed by
     * {@link #setConfig(AlertConfiguration)} followed by {@link #enableAlert(Class)}.
     * <p>
     * If it returns <code>false</code> it is equivalent to calling (for a predefined alert)
     * {@link #disableAlert(Class)} followed by {@link #setConfig(AlertConfiguration)}.
     * <p>
     * if an alert configuration was not previously set, the current setting will be used.
     * 
     * @param configurations
     *            the alert configurations (as varargs).
     * @throws AlertConfigurationException
     *             if the alert is already enabled (need to disable it first).
     * @see AlertConfigurationParser
     */
    void configure(AlertConfiguration... configurations) throws AlertConfigurationException;

    /**
     * Defines an alert configuration. Overrides all previously set properties for this alert.
     * <p>
     * The {@link AlertConfiguration#isEnabled()} is ignored. To enable, call {@link #enableAlert(Class)}.
     * To perform both operations in a single call, use {@link #configure(AlertConfiguration[])}.
     * <p>
     * An exception is raised if the alert is already enabled.
     * 
     * @param config
     *            the alert configuration
     * 
     * @throws AlertConfigurationException
     *             if the alert is already enabled (need to disable it first).
     */
    void setConfig(AlertConfiguration config) throws AlertConfigurationException;

    /**
     * Enable a previously set alert configuration. From this point, alerts will be triggered.
     * <p>
     * If the alert is already enabled, the request is silently ignored.
     * 
     * @param clazz
     *            the class of the alert configuration
     * @throws AlertConfigurationException
     *             if the alert can't be enabled for any of the following reasons:
     *             <ul>
     *             <li>if the configuration was not previously set.</li> <li>in the event of
     *             misconfiguration (such as failure to set an essential property).</li> <li>if
     *             initialization fails. </li>
     *             </ul>
     */
    void enableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException;

    /**
     * Disables a previously enabled alert. The configuration remains, but alerts will no longer be
     * triggered. The alert can be enabled at a later time, based on previously set configuration.
     * <p>
     * If the alert is already disabled, the request is silently ignored.
     * 
     * @param clazz
     *            the class of the alert configuration
     * 
     * @throws AlertConfigurationException
     *             if the alert configuration was not previously set.
     */
    void disableAlert(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException;

    /**
     * @return <code>true</code> if the alert is already enabled, <code>false</code> if the alert is
     *         disabled or if it's configuration was not found.
     * 
     * @param clazz
     *            the class of the alert configuration
     * 
     */
     boolean isAlertEnabled(Class<? extends AlertConfiguration> clazz);

    /**
     * Removes a previously set alert configuration.
     * <p>
     * An exception is raised if the alert is currently enabled. Disable the alert prior to this
     * call.
     * 
     * @param clazz
     *            the class of the alert configuration
     * 
     * @return <code>true</code> if removed ,<code>false</code> if it did not exist in the first
     *         place.
     * 
     * @throws AlertConfigurationException
     *             if the alert configuration can't be removed for any of the following reasons:
     *             <ul>
     *             <li>if the alert is currently enabled (need to disable it first).</li> <li>if the
     *             configuration was not previously set</li>
     *             </ul>
     */
    boolean removeConfig(Class<? extends AlertConfiguration> clazz) throws AlertConfigurationException;

    /**
     * Get the currently set alert configuration.
     * 
     * @param clazz
     *            the class of the alert configuration
     *            
     * @return The alert configuration implementation object set with the configuration properties.
     * 
     * @throws AlertConfigurationException
     *             if the alert configuration was not previously set.
     */
    <T extends AlertConfiguration> T getConfig(Class<T> clazz) throws AlertConfigurationException;
    
	/**
	 * Trigger an alert event for registered alert event listeners.
	 * @param alert an alert.
	 */
	void triggerAlert(Alert alert);
	
	/**
	 * @return the alert event manager to register/unregister for alert events.
	 */
	AlertTriggeredEventManager getAlertTriggered();
}
