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

import java.util.Arrays;
import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class StartedTooManyMachinesException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] agentUids;
    
    public StartedTooManyMachinesException(ProcessingUnit pu, Collection<GridServiceAgent> agents) {
        super(new String[]{pu.getName()}, "Started too many machines " + MachinesSlaUtils.machinesToString(agents)+". They are not needed by " + pu.getName());
        agentUids = createAgentUids(agents);
    }

    private static String[] createAgentUids(Collection<GridServiceAgent> agents) {
        String [] agentUids = new String[agents.size()];
        int i = 0;
        for (GridServiceAgent agent : agents) {
            agentUids[i] = agent.getUid();
            i++;
        }
        return agentUids;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(agentUids);
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
        StartedTooManyMachinesException other = (StartedTooManyMachinesException) obj;
        if (!Arrays.equals(agentUids, other.agentUids))
            return false;
        return true;
    }
    
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticMachineProvisioningFailureEvent event = new DefaultElasticMachineProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitNames(getAffectedProcessingUnits());
        return event;
    }
}
