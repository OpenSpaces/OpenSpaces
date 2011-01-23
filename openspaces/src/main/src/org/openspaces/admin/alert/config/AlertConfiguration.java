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

import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.parser.AlertConfigurationParser;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.openspaces.admin.bean.BeanConfig;

/**
 * A configuration of an alert trigger. The configuration consists of a weakly typed configuration
 * API based on String key-value pairs to configure an alert trigger. An enabled configuration means
 * that alerts may be triggered based on this configuration. A disabled configuration means that
 * alerts will not be triggered until enabled.
 * <p>
 * Implementors of this interface can provide more strongly typed API to enforce type-safety and
 * argument verifications.
 * <p>
 * An {@link AlertConfiguration} may be parsed by an {@link AlertConfigurationParser}. For example,
 * using the {@link XmlAlertConfigurationParser} which it's {@link AlertConfigurationParser#parse()}
 * method returns an array of configurations to be passed to
 * {@link AlertManager#configure(AlertConfiguration[])}.
 * 
 * @see AlertConfigurer
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertConfiguration extends BeanConfig {
    
    /**
     * @return <code>true</code> if this alert is enabled; <code>false</code> if this alert is
     *         currently disabled.
     */
    boolean isEnabled();
    
    /**
     * @param enabled
     *            <code>true</code> if this alert should be enabled; <code>false</code> if this
     *            alert should be disabled.
     */
    void setEnabled(boolean enabled);
}
