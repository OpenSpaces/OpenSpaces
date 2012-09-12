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
package org.openspaces.admin.config;

import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.memcached.config.MemcachedConfig;
import org.openspaces.admin.pu.config.ContextPropertyConfig;
import org.openspaces.admin.pu.config.MaxInstancesPerZoneConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.dependency.config.ProcessingUnitDependencyConfig;
import org.openspaces.admin.space.config.SpaceConfig;
import org.openspaces.core.config.xmlparser.AbstractXmlBeanNamespaceHandler;

/**
 * A spring namespace handler for the "admin" namespace.
 * see openspaces-admin.xsd
 * 
 * @author itaif
 * @since 9.0.1
 *
 */
public class AdminNamespaceHandler extends AbstractXmlBeanNamespaceHandler {

    @Override
    public void init() {
        register(ProcessingUnitConfig.class);
        register(SpaceConfig.class);
        register(MemcachedConfig.class);
        
        register(ProcessingUnitDependencyConfig.class);
        register(ApplicationConfig.class);
        register(ContextPropertyConfig.class);
        register(UserDetailsConfig.class);
        register(MaxInstancesPerZoneConfig.class);
    }
}
