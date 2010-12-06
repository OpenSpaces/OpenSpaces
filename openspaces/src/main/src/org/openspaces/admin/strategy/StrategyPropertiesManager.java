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

import java.util.Map;

/**
 * A weakly typed properties manager for managing Strategy configurations by their name. The
 * strategy key is the strategy bean class name (see
 * {@link StrategyConfig#getStartegyBeanClassName()})), mapped with the relevant configuration
 * properties.
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface StrategyPropertiesManager {

    /**
     * Adds a strategy by it's name with the corresponding configuration properties.
     * 
     * @param strategy
     *            the strategy name
     * @param properties
     *            the String key-value pairs configuration properties used to configure this
     *            strategy.
     * @throws StrategyAlreadyExistsException
     *             thrown if strategy has already been added to the manager.
     */
	void addStrategy(String strategy, Map<String,String> properties) throws StrategyAlreadyExistsException;

    /**
     * Sets a previously added strategy with new configuration properties. Overrides all previously
     * set properties.
     * 
     * @param strategy the strategy name
     * @param properties the String key-value pairs configuration properties used to configure this strategy.
     * @throws StrategyNotFoundException thrown if the strategy was not added/found in the manager.
     */
	void setStrategy(String strategy, Map<String,String> properties) throws StrategyNotFoundException;

    /**
     * Enable a previously added strategy. Creates the strategy bean corresponding to the strategy
     * name, configured with the properties previously set. If the strategy was already enabled, the
     * request will be ignored.
     * 
     * @param strategy
     *            the strategy name
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	void enableStrategy(String strategy) throws StrategyNotFoundException;

    /**
     * Disables a previously enabled strategy. The strategy object is discarded, but it's
     * configuration remains in the manager, and it can be enabled at a later time. If the strategy
     * was already disabled, the request will be ignored.
     * 
     * @param strategy
     *            the strategy name
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	void disableStrategy(String strategy) throws StrategyNotFoundException;

    /**
     * Removes a previously added strategy together with its configuration properties. This strategy
     * will need to be added again after it's removal.
     * 
     * @param strategy
     *            the strategy name
     * @throws StrategyNotFoundException
     *             thrown if the strategy was not added/found in the manager.
     */
	void removeStrategy(String strategy) throws StrategyNotFoundException;
	
	/**
	 * Get the configuration properties corresponding to the strategy.
	 * 
	 * @param strategy the strategy name
	 * @return the String key-value pairs configuration properties.
	 * @throws StrategyNotFoundException thrown if the strategy was not added/found in the manager.
	 */
	Map<String,String> getStrategy(String strategy) throws StrategyNotFoundException;;

    /**
     * Get all the strategies currently held by this manager.
     * 
     * @return an array of strategies held by this manager. If no strategies were added, an array of
     *         zero length is returned.
     */
	String[] getStrategies();

    /**
     * Get all the strategies which are currently enabled.
     * 
     * @see #enableStrategy(String)
     * @return an array of enabled strategies. If no strategies are enabled, an array of zero
     *         length is returned.
     */
	String[] getEnabledStrategies();

	/**
	 * Disable all currently enabled strategies.
	 * @see #disableStrategy(String)
	 */
	void disableAllStrategies();
}
