/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.transaction.manager.jta;

import java.lang.reflect.Field;
import java.util.Properties;

import com.gigaspaces.logger.GSLogConfigLoader;

/**
 * @author Dan Kilman
 */
public class LogConfigLoaderBean {

    private Properties overridedProperties;
    
    public LogConfigLoaderBean() { }
    
    public void override() throws Exception {
        nastyHackInOrderToReinitializeLoggingConfig();
        GSLogConfigLoader.getLoader(overridedProperties);
    }

    public void restore() throws Exception {
        nastyHackInOrderToReinitializeLoggingConfig();
        GSLogConfigLoader.getLoader();
    }
    
    public Properties getOverridedProperties() {
        return overridedProperties;
    }

    public void setOverridedProperties(Properties overridedProperties) {
        this.overridedProperties = overridedProperties;
    }
    
    private static void nastyHackInOrderToReinitializeLoggingConfig() throws Exception {
        Field field = GSLogConfigLoader.class.getDeclaredField("_loader");
        field.setAccessible(true);
        field.set(null, null);
    }
    
}
