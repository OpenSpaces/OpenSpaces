package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;
import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class StartedTooManyMachinesException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    private final String[] agentUids;
    
    public StartedTooManyMachinesException(ProcessingUnit pu, Collection<GridServiceAgent> agents) {
        super("Started too many machines " + MachinesSlaUtils.machinesToString(agents)+". They are not needed by " + pu.getName());
        this.affectedProcessingUnits = new String[] { pu.getName()};
        agentUids = createAgentUids(agents);
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }

    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof StartedTooManyMachinesException) {
            StartedTooManyMachinesException otherEx = (StartedTooManyMachinesException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits, this.affectedProcessingUnits) &&
                   Arrays.equals(otherEx.agentUids, this.agentUids);
        }
        return same;  
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
}
