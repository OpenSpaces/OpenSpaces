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
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.pu.MemberAliveIndicatorStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * A processing unit instance fault-detection alert triggered when a Grid Service Manager fails to
 * monitor a processing unit instance. The alert is first raised when the fault-detection mechanism
 * suspects that a service is down, and once more when all fault-detection retry attempts have been
 * exhausted. The alert is resolved if one the fault-detection retry attempts succeeded.
 * 
 * <p>
 * This alert will be received on the call to
 * {@link AlertTriggeredEventListener#alertTriggered(Alert)} for registered listeners.
 * 
 * @since 8.0.6
 * @author moran
 */
public class ProcessingUnitInstanceMemberAliveIndicatorAlert extends AbstractAlert {
    
    private static final long serialVersionUID = 1L;
    
    public static final String PROCESSING_UNIT_NAME = "processing-unit-name";
    public static final String MEMBER_ALIVE_INDICATOR_STATUS = "member-alive-indicator-status";
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String VIRTUAL_MACHINE_UID = "vm-uid";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    
    /** required by java.io.Externalizable */
    public ProcessingUnitInstanceMemberAliveIndicatorAlert() {
    }
    
    public ProcessingUnitInstanceMemberAliveIndicatorAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link ProcessingUnitInstance#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
    
    /**
     * {@inheritDoc}
     * The component description is equivalent to {@link ProcessingUnitInstance#getProcessingUnitInstanceName()}
     */
    @Override
    public String getComponentDescription() {
        return super.getComponentDescription();
    }
    
    /**
     * @return the processing unit name {@link ProcessingUnit#getName()}; may be <code>null</code>.
     */
    public String getProcessingUnitName() {
        return getProperties().get(PROCESSING_UNIT_NAME);
    }

    /**
     * @see #getComponentDescription()
     * @return the processing unit instance name
     *         {@link ProcessingUnitInstance#getProcessingUnitInstanceName()}.
     */
    public String getProcessingUnitInstanceName() {
        return getComponentDescription();
    }
    
    /**
     * @return the fault-detection status; may be <code>null</code>.
     */
    public MemberAliveIndicatorStatus getMemberAliveIndicatorStatus() {
        String value = getProperties().get(MEMBER_ALIVE_INDICATOR_STATUS);
        if (value == null) return null;
        return MemberAliveIndicatorStatus.valueOf(value);
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
     * The uid of the virtual machine that this alert corresponds to.
     * @return the virtual machine uid; may be <code>null</code>.
     */
    public String getVirtualMachineUid() {
        return getProperties().get(VIRTUAL_MACHINE_UID);
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
     * The heap memory utilization reading when this alert was fired.
     * @return the heap utilization; may be <code>null</code>.
     */
    public Double getHeapUtilization() {
        String value = getProperties().get(HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
}
