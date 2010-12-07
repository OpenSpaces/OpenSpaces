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
public interface BeanPropertiesManager {

    /**
     * Adds a bean by it's name with the corresponding configuration properties.
     * 
     * @param bean
     *            the bean name
     * @param properties
     *            the String key-value pairs configuration properties used to configure this
     *            bean.
     * @throws BeanAlreadyExistsException
     *             thrown if bean has already been added to the manager.
     */
	void addBean(String bean, Map<String,String> properties) throws BeanAlreadyExistsException;

    /**
     * Sets a previously added bean with new configuration properties. Overrides all previously
     * set properties.
     * 
     * @param bean the bean name
     * @param properties the String key-value pairs configuration properties used to configure this bean.
     * @throws BeanNotFoundException thrown if the bean was not added/found in the manager.
     */
	void setBean(String bean, Map<String,String> properties) throws BeanNotFoundException;

    /**
     * Enable a previously added bean. Creates the bean bean corresponding to the bean
     * name, configured with the properties previously set. If the bean was already enabled, the
     * request will be ignored.
     * 
     * @param bean
     *            the bean name
     * @throws BeanNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
	void enableBean(String bean) throws BeanNotFoundException;

    /**
     * Disables a previously enabled bean. The bean object is discarded, but it's
     * configuration remains in the manager, and it can be enabled at a later time. If the bean
     * was already disabled, the request will be ignored.
     * 
     * @param bean
     *            the bean name
     * @throws BeanNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
	void disableBean(String bean) throws BeanNotFoundException;

    /**
     * Removes a previously added bean together with its configuration properties. This bean
     * will need to be added again after it's removal.
     * 
     * @param bean
     *            the bean name
     * @throws BeanNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
	void removeBean(String bean) throws BeanNotFoundException;
	
	/**
	 * Get the configuration properties corresponding to the bean.
	 * 
	 * @param bean the bean name
	 * @return the String key-value pairs configuration properties.
	 * @throws BeanNotFoundException thrown if the bean was not added/found in the manager.
	 */
	Map<String,String> getBean(String bean) throws BeanNotFoundException;;

    /**
     * Get all the beans currently held by this manager.
     * 
     * @return an array of beans held by this manager. If no beans were added, an array of
     *         zero length is returned.
     */
	String[] getBeans();

    /**
     * Get all the beans which are currently enabled.
     * 
     * @see #enableBean(String)
     * @return an array of enabled beans. If no beans are enabled, an array of zero
     *         length is returned.
     */
	String[] getEnabledBeans();

	/**
	 * Disable all currently enabled beans.
	 * @see #disableBean(String)
	 */
	void disableAllBeans();
}
