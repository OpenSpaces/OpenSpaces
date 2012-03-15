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
package org.openspaces.pu.container.servicegrid.jmxs;

import java.util.Date;

import org.jini.rio.jsb.ServiceBeanAdapterMBean;

/**
 * Secured PU JMX MBean interface restricting access to the default MBean
 * {@link ServiceBeanAdapterMBean}.
 * 
 * @author Moran Avigdor
 */
public interface SecuredPUExtensionMBean {
    /**
     * Get the Date the ServiceBean was started
     */
    Date getStarted();

    /**
     * Get the discovery groups
     */
    String[] getLookupGroups();
}
