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
import org.openspaces.admin.alert.config.ReplicationRedoLogOverflowToDiskAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;

/**
 * A replication redo-log overflow to disk alert, fired when the redo-log exceeds the defined
 * redo-log memory capacity and overflows to disk. The alert is raised when the redo-log overflows.
 * The alert is resolved when the redo-log no longer uses the disk.
 * <p>
 * These thresholds can be configured by using the {@link ReplicationRedoLogOverflowToDiskAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogOverflowToDiskAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String VIRTUAL_MACHINE_UID = "vm-uid";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    
    public static final String REPLICATION_STATUS = "replication-status";
    public static final String REDO_LOG_SIZE = "redo-log-size";
    public static final String REDO_LOG_MEMORY_SIZE = "redo-log-memory-size";
    public static final String REDO_LOG_SWAP_SIZE = "redo-log-swap-size";

    /** required by java.io.Externalizable */
    public ReplicationRedoLogOverflowToDiskAlert() {
    }
    
    public ReplicationRedoLogOverflowToDiskAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link SpaceInstance#getUid()}
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
    
    /**
     * The {@link ReplicationStatus replication status} from source to target Space.
     * @return the replication status; may be <code>null</code>.
     */
    public String getReplicationStatus() {
        return getProperties().get(REPLICATION_STATUS);
    }
    
    /**
     * The total redo-log size (for all channels) for both memory and swap.
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogSize() {
        String value = getProperties().get(REDO_LOG_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The in-memory redo-log size (for all channels).
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogMemorySize() {
        String value = getProperties().get(REDO_LOG_MEMORY_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The swap redo-log size (for all channels).
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogSwapSize() {
        String value = getProperties().get(REDO_LOG_SWAP_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
}
