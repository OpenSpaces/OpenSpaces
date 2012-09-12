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

import org.openspaces.admin.internal.gsa.events.DefaultElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;


public class UnexpectedShutdownOfNewGridServiceAgentException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
    
    private static final long serialVersionUID = 1L;
    private final String machineUid;
    
    public UnexpectedShutdownOfNewGridServiceAgentException(Machine machine, String[] affectedProcessingUnits) {
        super(affectedProcessingUnits, "New machine " + MachinesSlaUtils.machineToString(machine) +
                " was started and the agent was also started, but then it was shutdown unexpectedly.");
        this.machineUid = machine.getUid();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((machineUid == null) ? 0 : machineUid.hashCode());
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
        UnexpectedShutdownOfNewGridServiceAgentException other = (UnexpectedShutdownOfNewGridServiceAgentException) obj;
        if (machineUid == null) {
            if (other.machineUid != null)
                return false;
        } else if (!machineUid.equals(other.machineUid))
            return false;
        return true;
    }
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticGridServiceAgentProvisioningFailureEvent event = new DefaultElasticGridServiceAgentProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitNames(getAffectedProcessingUnits());
        return event;
    }
}
