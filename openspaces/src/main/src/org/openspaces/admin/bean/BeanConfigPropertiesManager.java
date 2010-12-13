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

package org.openspaces.admin.bean;

import java.util.Map;

/**
 * A weakly typed properties manager for managing admin bean configurations by their name. The
 * String key is the bean class name (see {@link BeanConfig#getBeanClassName()})), mapped with the
 * relevant configuration properties.
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface BeanConfigPropertiesManager {

    /**
     * Adds a bean by it's name with the corresponding configuration properties.
     * 
     * @param beanClassName
     *            the bean name
     * @param properties
     *            the String key-value pairs configuration properties used to configure this
     *            bean.
     * @throws BeanConfigAlreadyExistsException
     *             thrown if bean has already been added to the manager.
     */
	void addConfig(String beanClassName, Map<String,String> properties) throws BeanConfigAlreadyExistsException;

    /**
     * Sets a previously added bean with new configuration properties. Overrides all previously
     * set properties.
     * 
     * @param beanClassName the bean name
     * @param properties the String key-value pairs configuration properties used to configure this bean.
     * @throws BeanConfigNotFoundException thrown if the bean was not added/found in the manager.
     */
	void setConfig(String beanClassName, Map<String,String> properties) throws BeanConfigNotFoundException;

    /**
     * Enable a previously added bean's configuration. Creates the bean bean corresponding to the
     * bean name, configured with the properties previously set. If the bean was already enabled,
     * the request will be ignored.
     * 
     * @param beanClassName
     *            the bean name
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     * @throws BeanConfigException
     *             if the request to enable a bean can't be fulfilled.
     * @throws BeanConfigurationException
     *             in the event of misconfiguration (such as failure to set an essential property).
     * @throws BeanInitializationException
     *             if initialization fails.
     */
	void enableConfig(String beanClassName) throws BeanConfigNotFoundException, BeanConfigException;

    /**
     * Disables a previously enabled bean's configuration. The bean object is discarded, but it's
     * configuration remains in the manager, and it can be enabled at a later time. If the bean was
     * already disabled, the request will be ignored.
     * 
     * @param beanClassName
     *            the bean name
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
	void disableConfig(String beanClassName) throws BeanConfigNotFoundException;

    /**
     * Removes a previously added bean together with its configuration properties. This bean
     * will need to be added again after it's removal.
     * 
     * @param beanClassName
     *            the bean name
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
	void removeConfig(String beanClassName) throws BeanConfigNotFoundException;
	
	/**
	 * Get the configuration properties corresponding to the bean.
	 * 
	 * @param beanClassName the bean name
	 * @return the String key-value pairs configuration properties.
	 * @throws BeanConfigNotFoundException thrown if the bean was not added/found in the manager.
	 */
	Map<String,String> getConfig(String beanClassName) throws BeanConfigNotFoundException;;

    /**
     * Get all the bean's class names currently held by this manager.
     * 
     * @return an array of bean class names held by this manager. If no beans were added, an array
     *         of zero length is returned.
     */
	String[] getBeans();

    /**
     * Get all the class names of beans which are currently enabled.
     * 
     * @see #enableConfig(String)
     * @return an array of enabled beans. If no beans are enabled, an array of zero
     *         length is returned.
     */
	String[] getEnabledBeans();

	/**
	 * Disable all currently enabled bean configurations.
	 * @see #disableConfig(String)
	 */
	void disableAllBeans();
}
