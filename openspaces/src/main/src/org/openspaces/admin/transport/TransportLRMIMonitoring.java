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
package org.openspaces.admin.transport;

import com.gigaspaces.lrmi.LRMIServiceMonitoringDetails;

/**
 * Provide low level monitoring capabilities of the transport layer
 * @author eitany
 * @since 9.1
 */
public interface TransportLRMIMonitoring {

	/**
     * Enables lrmi monitoring (gigaspaces internal remoting layer), this will cause the target transport to start tracking lrmi
     * activity which can later be viewed by calling.
     * {@link #fetchServicesMonitoringDetails()}
     */
    void enableMonitoring();
    
    /**
     * Disabled lrmi monitoring (gigaspaces internal remoting layer).
     * {@link #enableMonitoring()}
     */
    void disableMonitoring();
    
    /**
     * Return lrmi (gigaspaces internal remoting layer) services monitoring details. This will only work if 
     * lrmi monitoring was previously enabled, either by admin API, Jmx or system property. This will include monitoring
     * details of incoming invocation on hosted service within the targeted transport (Jvm).
     * {@link #enableMonitoring()}
     */
    LRMIServiceMonitoringDetails[] fetchServicesMonitoringDetails();
}
