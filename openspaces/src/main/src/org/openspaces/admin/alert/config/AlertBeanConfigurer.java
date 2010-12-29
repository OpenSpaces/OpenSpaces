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

import org.openspaces.admin.bean.BeanConfigurer;

/**
 * A weakly-typed configuration API based on the 'builder' patterns for a more convenient
 * code-fluent approach to configuring an {@link AlertBeanConfig}.
 * <p>
 * The fully configured {@link AlertBeanConfig} object is returned by the call to
 * {@link #getConfig()}.
 * <p>
 * By default, the configuration is empty - has no properties set. The
 * {@link #applyRecommendedSettings()} can be used to set the recommended setting for all
 * configuration properties.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertBeanConfigurer extends BeanConfigurer<AlertBeanConfig> {
}
