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
import org.openspaces.admin.alert.config.HeapMemoryUtilizationAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.vm.VirtualMachine;

/**
 * A heap memory utilization alert, fired upon triggered JVM heap-memory thresholds. The alert is
 * raised when heap memory crosses a 'high' threshold for a specified period of time. The alert is
 * resolved when heap-memory crosses a 'low' threshold for a specified period of time.
 * <p>
 * These thresholds can be configured by using the {@link HeapMemoryUtilizationAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class HeapMemoryUtilizationAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    
    public static final String PROCESS_ID = "process-id";
    public static final String COMPONENT_NAME = "component-name";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    public static final String MAX_HEAP_IN_BYTES = "max-heap-in-bytes";

    /** required by java.io.Externalizable */
    public HeapMemoryUtilizationAlert() {
    }
    
    public HeapMemoryUtilizationAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link VirtualMachine#getUid()}
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
     * The process id of the component for which the alert was fired.
     * @return the process id (pid); may be <code>null</code>.
     */
    public String getProcessId() {
        return getProperties().get(PROCESS_ID);
    }

    /**
     * The name of the component for which the alert was fired (e.g. 'Grid Service Agent', 'Grid
     * Service Manager', 'Grid Service Container', 'Lookup Service').
     * 
     * @return the name of the component.
     */
    public String getComponentName() {
        return getProperties().get(COMPONENT_NAME);
    }
    
    /**
     * The Heap utilization reading when this alert was fired.
     * @return the Heap utilization; may be <code>null</code>.
     */
    public Double getHeapUtilization() {
        String value = getProperties().get(HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The maximum amount of memory in bytes that can be used for memory management. This method
     * returns -1 if the maximum memory size (-Xmx) is undefined.
     * 
     * @return the Heap utilization; may be <code>null</code>.
     */
    public Long getMaxHeapInBytes() {
        String value = getProperties().get(MAX_HEAP_IN_BYTES);
        if (value == null) return null;
        return Long.valueOf(value);
    }
}
