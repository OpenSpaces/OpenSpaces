package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.machine.Machine;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;


public class UnexpectedShutdownOfNewMachineException extends MachinesSlaEnforcementInProgressException {
    
    private static final long serialVersionUID = 1L;
    
    public UnexpectedShutdownOfNewMachineException(Machine machine) {
        super("New machine " + MachinesSlaUtils.machineToString(machine) +
                " was started and the agent was also started, but then it was shutdown unexpectedly.");
    }
}
