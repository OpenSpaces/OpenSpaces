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

import org.openspaces.core.bean.Bean;

/**
 * A weakly typed configuration API based on String key-value pairs to configure an admin
 * <tt>Bean</tt>. Implementors of this interface can provide more strongly typed API to enforce
 * type-safety and argument verifications.
 * <p>
 * The <tt>BeanConfig</tt> is a client side configuration. The String key-value pairs returned by
 * the {@link #getProperties()} method, are sent to the server to be injected into the admin
 * <tt>Bean</tt> matching the {@link #getBeanClassName() bean class-name}.
 * <p>
 * By default, the configuration is empty - has no properties set. The recommended setting for the
 * configuration properties should be derived from the javadoc.
 * 
 * @see BeanConfigurer
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface BeanConfig {
	
    /**
     * The {@link Bean} implementation class name corresponding to this <tt>BeanConfig</tt>.
     * 
     * @return the name of the admin <tt>Bean</tt> implementation class.
     */
	String getBeanClassName();

    /**
     * Set with String key-value pairs to configure properties belonging to this bean. Overrides
     * all previously set properties.
     * 
     * @param properties the properties to configure this bean object.
     */
	void setProperties(Map<String,String> properties);

    /**
     * Get the String key-value pairs properties used to configure this bean.
     * 
     * @return the properties used to configure this bean object.
     */
	Map<String,String> getProperties();
}
