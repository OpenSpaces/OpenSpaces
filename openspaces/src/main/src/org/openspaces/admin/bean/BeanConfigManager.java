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

/**
 * A strongly typed bean configuration manager using the strongly typed implementation of a
 * {@link BeanConfig}. It rides on top of the weakly typed bean configuration properties manager
 * (see {@link BeanConfigPropertiesManager}). Underneath this wrapper, the bean name (
 * {@link BeanConfig#getBeanClassName()}) and the configuration properties (
 * {@link BeanConfig#getProperties()}) are extracted and passed to the bean properties manager.
 * 
 * @param <B>
 *            the bean configuration implementation class
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface BeanConfigManager<B extends BeanConfig> {

    /**
     * Defines a configuration for the specified bean. Overrides all previously set properties for that bean.
     * The configuration object internally holds String key-value pairs used to configure this bean.
     * <p>
     * An exception is raised if the bean is enabled.
     * 
     * @param config
     *            the bean configuration
     *            
     * @throws EnabledBeanConfigCannotBeChangedException - if the bean is enabled
     */
    void setBeanConfig(B config) throws BeanConfigNotFoundException;

    /**
     * Enable a previously added bean configuration. 
     * Creates the bean instance with the previously set properties.
     * If the bean is already enabled, the request is silently ignored.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     * @throws BeanConfigurationException
     *             in the event of misconfiguration (such as failure to set an essential property).
     * @throws BeanInitializationException
     *             if bean initialization fails.
     */
    <T extends B> void enableBean(Class<T> clazz) throws BeanConfigNotFoundException, BeanConfigurationException, BeanInitializationException;

    /**
     * Disables a bean. 
     * The bean object is discarded but it's configuration remains. 
     * The bean can be enabled at a later time.
     * <p> 
     * If the bean is already disabled, the request is silently ignored.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     *            
     * @throws BeanConfigNotFoundException
     *              bean configuration cannot be found.
     */
    <T extends B> void disableBean(Class<T> clazz) throws BeanConfigNotFoundException;

    /**
     * @return true if the bean is enabled, false if the bean is disabled
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     *            
     */
    <T extends B> boolean isBeanEnabled(Class<T> clazz);
    
    /**
     * Removes a bean configuration. 
     * <p>
     * An exception is raised if the bean is enabled.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     *            
     * @return true if removed ,false if it did not exist in the first place.
     * 
     * @throws EnabledBeanConfigCannotBeChangedException
     *              The bean is enabled. Disable it first.
     */
    <T extends B> boolean removeBeanConfig(Class<T> clazz) throws EnabledBeanConfigCannotBeChangedException;

    /**
     * Get the bean configuration represented by the specified configuration class.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     *            
     * @return The bean configuration implementation object set with the configuration properties.
     * 
     * @throws BeanConfigNotFoundException
     *             Bean configuration cannot be found. Put the configuration first.
     */
    <T extends B> T getBeanConfig(Class<T> clazz) throws BeanConfigNotFoundException;
    
}