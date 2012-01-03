package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;

import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;


public class UnexpectedShutdownOfNewGridServiceAgentException extends GridServiceAgentSlaEnforcementInProgressException implements SlaEnforcementFailure {
    
    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    private final String machineUid;
    
    public UnexpectedShutdownOfNewGridServiceAgentException(Machine machine, String[] affectedProcessingUnits) {
        super("New machine " + MachinesSlaUtils.machineToString(machine) +
                " was started and the agent was also started, but then it was shutdown unexpectedly.");
        this.affectedProcessingUnits = affectedProcessingUnits;
        this.machineUid = machine.getUid();
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof UnexpectedShutdownOfNewGridServiceAgentException) {
            UnexpectedShutdownOfNewGridServiceAgentException otherEx = (UnexpectedShutdownOfNewGridServiceAgentException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits, this.affectedProcessingUnits) &&
                   otherEx.machineUid.equals(this.machineUid);
        }
        return same;  
    }
}
