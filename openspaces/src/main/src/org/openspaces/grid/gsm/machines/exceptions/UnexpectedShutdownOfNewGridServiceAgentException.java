package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;


public class UnexpectedShutdownOfNewGridServiceAgentException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
    
    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public UnexpectedShutdownOfNewGridServiceAgentException(Machine machine, String[] affectedProcessingUnits) {
        super("New machine " + MachinesSlaUtils.machineToString(machine) +
                " was started and the agent was also started, but then it was shutdown unexpectedly.");
        this.affectedProcessingUnits = affectedProcessingUnits;
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
}
