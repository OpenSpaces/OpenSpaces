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
package org.openspaces.grid.gsm.machines;

import java.util.Set;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.machines.exceptions.NeedToWaitUntilAllGridServiceAgentsDiscoveredException;
import org.openspaces.grid.gsm.machines.exceptions.SomeProcessingUnitsHaveNotCompletedStateRecoveryException;
import org.openspaces.grid.gsm.machines.exceptions.UndeployInProgressException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpoint;


/**
 * A service that on demand enforces the specified number of machines. 
 *  
 * @author itaif
 *
 * @see CapacityMachinesSlaPolicy
 */
public interface MachinesSlaEnforcementEndpoint extends ServiceLevelAgreementEnforcementEndpoint{
    
    void enforceSla(EagerMachinesSlaPolicy sla) throws GridServiceAgentSlaEnforcementInProgressException;
    
    void enforceSla(CapacityMachinesSlaPolicy sla) throws MachinesSlaEnforcementInProgressException, GridServiceAgentSlaEnforcementInProgressException;
    
    /**
     * @return a list of agents for this pu including memory/cpu for each. 
     * Non-discovered agents are removed from returned capacity
     */
    CapacityRequirementsPerAgent getAllocatedCapacityFilterUndiscoveredAgents(AbstractMachinesSlaPolicy sla);

    /**
     * @return a list of agents for this pu including memory/cpu for each. 
     * Non-discovered agents throw IllegalStateException
     */
    CapacityRequirementsPerAgent getAllocatedCapacity(AbstractMachinesSlaPolicy sla);
    
    /**
     * Recover state for the specified SLA.
     * @throws UndeployInProgressException 
     * @since 9.1.0
     */
    void recoverStateOnEsmStart(AbstractMachinesSlaPolicy sla) throws SomeProcessingUnitsHaveNotCompletedStateRecoveryException, NeedToWaitUntilAllGridServiceAgentsDiscoveredException, UndeployInProgressException;
    
    /**
     * Mark that the specified processing unit has recovered state.
     * @since 9.1.0
     */
    void recoveredStateOnEsmStart(ProcessingUnit processingUnit);

    /**
     * @return true if the specified processing unit has recovered state
     * @since 9.1.0
     */
    boolean isRecoveredStateOnEsmStart(ProcessingUnit processingUnit);

    /**
     * @return a list of zones that are being tracked for the specified processing unit
     */
    Set<ZonesConfig> getGridServiceAgentsZones();

    /**
     * Replaces the allocated capacity of the specified sla zones, with the allocated capacity
     * @return true if actually changed anything in the allocation
     */
    boolean replaceAllocatedCapacity(AbstractMachinesSlaPolicy sla);

    /**
     * Erases all Allocated (state) that is related to the specified processing unit
     * @see #isAllocatedCapacityRemoved()
     */
    void removeUndeployedProcessingUnit(ProcessingUnit pu);
}
