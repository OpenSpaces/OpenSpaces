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
package org.openspaces.admin.strategy;

/**
 * A strongly typed strategy manager using the strongly typed implementation of a {@link StrategyConfig}. It rides on top of the
 * weakly typed strategy properties manager (see {@link StrategyPropertiesManager}). The strategy name ({@link StrategyConfig#getStartegyBeanClassName()})
 * and the configuration properties ({@link StrategyConfig#getProperties()}) are extracted and passed to the properties manager.
 * 
 * @param <S> the strategy configuration implementation class
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface StrategyManager<S extends StrategyConfig> {

    /**
     * Adds a strategy by it's name {@link StrategyConfig#getStartegyBeanClassName()} with the
     * corresponding configuration properties. The configuration object is filled with String
     * key-value pairs configuration properties used to configure this strategy.
     * 
     * @param config
     *            the strategy configuration
     * @throws StrategyAlreadyExistsException
     *             thrown if strategy has already been added to the manager.
     */
    void addStrategy(S config)  throws StrategyAlreadyExistsException;

    /**
     * Sets a previously added strategy with new configuration properties. Overrides all previously
     * set properties. The configuration object is filled with String key-value pairs configuration
     * properties used to configure this strategy.
     * 
     * @param config
     *            the strategy configuration
     * @throws StrategyNotFoundException
     *             thrown if strategy has already been added to the manager.
     */
    void setStrategy(S config) throws StrategyNotFoundException;
    
    /**
     * Enable a previously added strategy. Creates the strategy bean corresponding to the strategy
     * name, configured with the properties previously set. If the strategy was already enabled, the
     * request will be ignored.
     * 
     * @param <T>
     *            a strategy configuration implementation
     * @param clazz
     *            the class of the strategy configuration
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	<T extends S> void enableStrategy(Class<T> clazz) throws StrategyNotFoundException;

    /**
     * Disables a previously enabled strategy. The strategy object is discarded, but it's
     * configuration remains in the manager, and it can be enabled at a later time. If the strategy
     * was already disabled, the request will be ignored.
     * 
     * @param <T>
     *            a strategy configuration implementation
     * @param clazz
     *            the class of the strategy configuration
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	<T extends S> void disableStrategy(Class<T> clazz) throws StrategyNotFoundException;

    /**
     * Removes a previously added strategy together with its configuration properties. This strategy
     * will need to be added again after it's removal.
     * 
     * @param <T>
     *            a strategy configuration implementation
     * @param clazz
     *            the class of the strategy configuration
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	<T extends S> void removeStrategy(Class<T> clazz) throws StrategyNotFoundException;

    /**
     * Get the configuration properties corresponding to the strategy.
     * 
     * @param <T>
     *            a strategy configuration implementation
     * @param clazz
     *            the class of the strategy configuration
     * @return The strategy configuration implementation object set with the configuration
     *         properties.
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	<T extends S> T getStrategy(Class<T> clazz) throws StrategyNotFoundException;
}