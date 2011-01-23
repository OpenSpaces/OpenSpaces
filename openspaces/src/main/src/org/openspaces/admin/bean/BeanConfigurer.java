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
 * A weakly-typed configuration API based on the 'builder' patterns for a more convenient
 * code-fluent approach to configuring a {@link BeanConfig}.
 * <p>
 * The fully configured {@link BeanConfig} object is returned by the call to {@link #create()}.
 * <p>
 * By default, the configuration is empty - has no properties set. The recommended setting for the
 * configuration properties should be derived from the javadoc.
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface BeanConfigurer<T extends BeanConfig> {
    
    /**
     * Get the fully configured {@link BeanConfig} object (after all properties have been set).
     * 
     * @return a fully configured <tt>BeanConfig</tt>.
     */
	T create();
}
