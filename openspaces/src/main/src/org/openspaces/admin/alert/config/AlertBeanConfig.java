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

package org.openspaces.admin.alert.config;

import org.openspaces.admin.bean.BeanConfig;

/**
 * A weakly typed configuration API based on String key-value pairs to configure an admin
 * <tt>AlertBean</tt>. Implementors of this interface can provide more strongly typed API to enforce
 * type-safety and argument verifications.
 * <p>
 * The <tt>AlertBeanConfig</tt> is a client side configuration. The String key-value pairs returned
 * by the {@link #getProperties()} method, are sent to the server to be injected into the admin
 * <tt>AlertBean</tt> matching the {@link #getBeanClassName() bean class-name}.
 * <p>
 * By default, the configuration is empty - has no properties set. The
 * {@link #applyRecommendedSettings()} can be used to set the recommended setting for all
 * configuration properties.
 * 
 * @see AlertBeanConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertBeanConfig extends BeanConfig {
}
