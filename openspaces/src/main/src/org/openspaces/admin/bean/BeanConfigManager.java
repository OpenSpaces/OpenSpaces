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
     * Adds configuration properties of a bean. The bean is referenced by it's name - given by
     * {@link BeanConfig#getBeanClassName()}. The configuration object is filled with String
     * key-value configuration property pairs used to configure this bean.
     * 
     * @param config
     *            the bean configuration
     * @throws BeanConfigAlreadyExistsException
     *             thrown if bean has already been added to the manager.
     */
    void addConfig(B config) throws BeanConfigAlreadyExistsException;

    /**
     * Sets a previously added bean with new configuration properties. Overrides all previously set
     * properties. The configuration object is filled with String key-value pairs configuration
     * properties used to configure this bean.
     * <p>
     * If the bean is already enabled, the new configuration will 'restart' the bean with the newly
     * set configuration (same as calling {@link #disableConfig(Class)}, followed by
     * {@link #setConfig(BeanConfig)}, and {@link #enableConfig(Class)}).
     * 
     * @param config
     *            the bean configuration
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
    void setConfig(B config) throws BeanConfigNotFoundException;

    /**
     * Enable a previously added bean configuration. Creates the bean corresponding to the bean
     * name, configured with the properties previously set. If the bean was already enabled, the
     * request will be ignored.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     * @throws BeanConfigException
     *             if the request to enable a bean can't be fulfilled.
     * @throws BeanConfigurationException
     *             in the event of misconfiguration (such as failure to set an essential property).
     * @throws BeanInitializationException
     *             if initialization fails.
     */
    <T extends B> void enableConfig(Class<T> clazz) throws BeanConfigNotFoundException, BeanConfigException;

    /**
     * Disables a previously enabled bean configuration. The bean object is discarded, but it's
     * configuration remains in the manager, and it can be enabled at a later time. If the bean was
     * already disabled, the request will be ignored.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
    <T extends B> void disableConfig(Class<T> clazz) throws BeanConfigNotFoundException;

    /**
     * Removes a previously added bean together with its configuration properties. This bean will
     * need to be added again after it's removal.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
    <T extends B> void removeConfig(Class<T> clazz) throws BeanConfigNotFoundException;

    /**
     * Get the configuration properties corresponding to the bean represented by this configuration
     * class.
     * 
     * @param <T>
     *            a bean configuration implementation (see {@link BeanConfig})
     * @param clazz
     *            the class of the bean configuration
     * @return The bean configuration implementation object set with the configuration properties.
     * @throws BeanConfigNotFoundException
     *             thrown if the bean was not added/found in the manager.
     */
    <T extends B> T getConfig(Class<T> clazz) throws BeanConfigNotFoundException;
}