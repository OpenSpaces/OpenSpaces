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
package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.PhysicalMemoryUtilizationAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.os.OperatingSystem;

/**
 * A physical memory utilization alert, fired upon triggered machine physical memory thresholds. The
 * alert is raised when memory crosses a 'high' threshold for a specified period of time. The alert
 * is resolved when memory crosses a 'low' threshold for a specified period of time.
 * <p>
 * These thresholds can be configured by using the {@link PhysicalMemoryUtilizationAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class PhysicalMemoryUtilizationAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String MEMORY_UTILIZATION = "memory-utilization";

    /** required by java.io.Externalizable */
    public PhysicalMemoryUtilizationAlert() {
    }
    
    public PhysicalMemoryUtilizationAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link OperatingSystem#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
    
    /**
     * The host address of the machine that this alert corresponds to.
     * @return the host address; may be <code>null</code>.
     */
    public String getHostAddress() {
        return getProperties().get(HOST_ADDRESS);
    }
    
    /**
     * The host name of the machine that this alert corresponds to.
     * @return the host name; may be <code>null</code>.
     */
    public String getHostName() {
        return getProperties().get(HOST_NAME);
    }
    
    /**
     * The CPU utilization reading when this alert was fired.
     * @return the CPU utilization; may be <code>null</code>.
     */
    public Double getCpuUtilization() {
        String value = getProperties().get(CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The memory utilization reading when this alert was fired.
     * @return the memory utilization; may be <code>null</code>.
     */
    public Double getMemoryUtilization() {
        String value = getProperties().get(MEMORY_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
}
