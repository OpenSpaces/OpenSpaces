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
     * Defines a configuration for the specified bean. Overrides all previously set properties for that bean.
     * <p>
     * An exception is raised if the bean is enabled.
     * 
     * @param beanClassName the bean class name
     * @param properties the String key-value pairs used to configure this bean.
     * 
     * @throws EnabledBeanConfigCannotBeChangedException
     *      The bean is enabled. Disable it first.
     */
	void setBeanConfig(String beanClassName, Map<String,String> properties) throws EnabledBeanConfigCannotBeChangedException;

    /**
     * Enables a previously added bean. 
     * Creates the bean instance with the previously set properties.
     * <p> 
     * If the bean is already enabled, the request is silently ignored.
     * 
     * @param beanClassName
     *            the bean class name
     *            
     * @throws BeanConfigNotFoundException
     *             thrown if the bean configuration is not found.
     * @throws BeanConfigurationException
     *             in the event of misconfiguration (such as failure to set an essential property).
     * @throws BeanInitializationException
     *             if bean initialization fails.
     */
	void enableBean(String beanClassName) throws BeanConfigNotFoundException, BeanConfigurationException, BeanInitializationException;

    /**
     * Disables a bean. 
     * The bean object is discarded but it's configuration remains. 
     * The bean can be enabled at a later time.
     * <p> 
     * If the bean is already disabled, the request is silently ignored.
     * 
     * @param beanClassName
     *            the bean class name
     *            
     * @throws BeanConfigNotFoundException
     *             bean configuration cannot be found.
     */
	void disableBean(String beanClassName) throws BeanConfigNotFoundException;

	/**
	 * @return true if the bean is enabled, false if the bean is disabled
	 * 
	 * @param beanClassName
     *            the bean class name
     *            
	 */
	boolean isBeanEnabled(String beanClassName);

    /**
     * Removes a bean configuration. 
     * <p>
     * An exception is raised if the bean is enabled.
     * 
     * @param beanClassName
     *            the bean class name
     * @return true if removed ,false if it did not exist in the first place.
     * 
     * @throws EnabledBeanConfigCannotBeChangedException 
     *         The bean is enabled. Disable it first.
     */
	boolean removeBeanConfig(String beanClassName) throws EnabledBeanConfigCannotBeChangedException;
	
	/**
	 * Get the bean configuration.
	 * 
	 * @param beanClassName the bean class name
	 * 
	 * @return the String key-value pairs configuration properties.
	 * 
	 * @throws BeanConfigNotFoundException 
	 *         Bean configuration cannot be found. Put the configuration first.
	 */
	Map<String,String> getBeanConfig(String beanClassName) throws BeanConfigNotFoundException;;

    /**
     * Lists the class names of configured beans.
     * 
     * @return an array of bean class names that have been configured bean.
     *         If no bean configuration exists, a zero length array is returned.
     */
	String[] getBeansClassNames();

    /**
     * Lists the class names of enabled beans.
     * 
     * @see #enableBean(String)
     * @return an array of enabled beans. 
     *         If no beans are enabled, a zero length array is returned.
     */
	String[] getEnabledBeansClassNames();

	/**
	 * Disable all currently enabled beans.
	 * @see #disableBean(String)
	 */
	void disableAllBeans();
}
