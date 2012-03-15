/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.admin.alert.config.parser;

import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.AlertConfiguration;

/**
 * A configuration parser for setting a bulk of alert configurations using the call to
 * {@link AlertManager#configure(AlertConfiguration[])}.
 * <p>
 * @see XmlAlertConfigurationParser which reads from an xml file the alert configurations.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public interface AlertConfigurationParser {

    /**
     * Parse and return an array of {@link AlertConfiguration}.
     * <p>
     * <ul>
     * <li>{@link AlertConfiguration#getProperties()} - String key-value properties used to
     * configure the alert.</li>
     * <li>{@link AlertConfiguration#isEnabled()} - enable the configuration for this alert.</li>
     * <li>{@link AlertConfiguration#getBeanClassName()} - the server-side <tt>alert trigger</tt>
     * class name.</li>
     * </ul>
     * 
     * @return a non-null array of {@link AlertConfiguration}s.
     * @throws AlertConfigurationParserException
     *             if failed to parse.
     */
    AlertConfiguration[] parse() throws AlertConfigurationParserException;
}
