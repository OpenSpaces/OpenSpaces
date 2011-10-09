package org.openspaces.grid.gsm.machines.exceptions;


public class FailedToStartNewMachineException extends MachinesSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public FailedToStartNewMachineException(Exception cause) {
        super("Machine provisioning failed to start a new machine.",cause);
    }

}
