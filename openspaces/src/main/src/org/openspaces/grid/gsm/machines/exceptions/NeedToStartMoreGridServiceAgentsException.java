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
package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Map;
import java.util.Map.Entry;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.MachineCapacityRequirements;
import org.openspaces.grid.gsm.machines.AbstractMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementState.StateKey;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class NeedToStartMoreGridServiceAgentsException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
    
    private static final long serialVersionUID = 1L;
    private final CapacityRequirements capacityShortage;

    public NeedToStartMoreGridServiceAgentsException(AbstractMachinesSlaPolicy sla, MachinesSlaEnforcementState state, CapacityRequirements capacityShortage, ProcessingUnit pu) {
        super(new String[] { pu.getName()}, createMessage(sla, state, capacityShortage, pu));
        this.capacityShortage = capacityShortage;
    }

    public NeedToStartMoreGridServiceAgentsException(CapacityRequirements capacityShortage, ProcessingUnit pu) {
        super(new String[] { pu.getName()}, createBasicMessage(capacityShortage));
        this.capacityShortage = capacityShortage;
    }
    
    private static String createBasicMessage(CapacityRequirements capacityShortage) {
        return "Cannot enforce Machines SLA since there are not enough machines available. "+
                "Need more capacity: " + capacityShortage;
    }
    
    private static String createMessage(AbstractMachinesSlaPolicy sla, MachinesSlaEnforcementState state, CapacityRequirements capacityShortage, ProcessingUnit pu) {
        return createBasicMessage(capacityShortage) + ". " + reportToString(sla, state.groupCapacityPerProcessingUnitPerAgent(getKey(pu,sla)));
    }

    private static String reportToString(
            AbstractMachinesSlaPolicy sla, Map<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> createReportOfAllMachines) {
        StringBuilder message = new StringBuilder("Capacity Report of all relevant machines:");
        for (Entry<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> agentpair : createReportOfAllMachines.entrySet()) {
            
            GridServiceAgent agent = agentpair.getKey();
            MachineCapacityRequirements total = new MachineCapacityRequirements(agent.getMachine());
            
            CapacityRequirements free = total;
            
            // add to the report the amount of reserved capacity per machine
            //"reserved-capacity", sla.getReservedCapacityPerMachine());
            String ipAddress = MachinesSlaUtils.machineToString(agent.getMachine());
            CapacityRequirements reserved = sla.getReservedCapacityPerMachine();
            message
                .append("\"").append(ipAddress).append("\":{")
                .append("total:{").append(total).append("},")
                .append("reserved:{").append(reserved).append("},");
            free = free.subtractOrZero(reserved);
            for (Entry<ProcessingUnit,CapacityRequirements> pupair : agentpair.getValue().entrySet()) {
                String puName = pupair.getKey().getName();
                CapacityRequirements capacity = pupair.getValue();
                message.append(puName).append(":{").append(capacity.toString()).append("},");
                free = free.subtractOrZero(capacity);
            }
            message.append("free:").append(free).append("},");
        }
        int lastIndex = message.length()-1;
        if (message.charAt(lastIndex)==',') {
            message.deleteCharAt(lastIndex);
        }
        return message.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((capacityShortage == null) ? 0 : capacityShortage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NeedToStartMoreGridServiceAgentsException other = (NeedToStartMoreGridServiceAgentsException) obj;
        if (capacityShortage == null) {
            if (other.capacityShortage != null)
                return false;
        } else if (!capacityShortage.equals(other.capacityShortage))
            return false;
        return true;
    }
    

    private static StateKey getKey(ProcessingUnit pu, AbstractMachinesSlaPolicy sla) {
        return new MachinesSlaEnforcementState.StateKey(pu, sla.getExactGridServiceAgentZones());
    }
}
