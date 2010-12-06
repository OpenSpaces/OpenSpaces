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

import org.openspaces.admin.internal.strategy.StrategyBean;

/**
 * The strategy pattern is intended to provide a means to define a set of algorithms/policies, each
 * one encapsulated as an object. The strategy implementation is considered to be at the "server"
 * side, while it's configuration object is at the "client" side. The <tt>StrategyConfig</tt>
 * defines the configuration details of a corresponding {@link StrategyBean} implementation.
 * <p>
 * A weakly typed configuration API based on String key-value pairs to configure a Strategy.
 * Implementors of this interface can provide more strongly typed API to enforce type-safety and
 * argument verifications.
 * <p>
 * The <tt>StrategyConfig</tt> is a client side configuration. The String key-value pairs returned
 * by the {@link #getProperties()} method, are sent to the server to be injected into the
 * {@link StrategyBean} matching the {@link #getStartegyBeanClassName() strategy bean class-name}.
 * <p>
 * By default, the configuration is empty - has no properties set. The
 * {@link #applyRecommendedSettings()} can be used to set the recommended setting for all
 * configuration properties.
 * 
 * @see StrategyConfigurer
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface StrategyConfig {
	
    /**
     * The {@link StrategyBean} implementation class name corresponding to this <tt>StrategyConfig</tt>.
     * 
     * @return the name of the <tt>StrategyBean</tt> implementation class.
     */
	String getStartegyBeanClassName();

    /**
     * Apply the recommended settings for all the configuration properties. Overrides any previously
     * set property value.
     */
	void applyRecommendedSettings();

    /**
     * Set with String key-value pairs to configure properties belonging to this strategy. Overrides
     * all previously set properties.
     * 
     * @param properties the properties to configure this strategy object.
     */
	void setProperties(Map<String,String> properties);

    /**
     * Get the String key-value pairs properties used to configure this strategy.
     * 
     * @return the properties used to configure this strategy object.
     */
	Map<String,String> getProperties();
}
