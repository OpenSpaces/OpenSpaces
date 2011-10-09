package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class MachinesSlaEnforcementInProgressException extends SlaEnforcementInProgressException{

    private static final long serialVersionUID = 1L;
    
    public MachinesSlaEnforcementInProgressException() {
        super("Machines SLA Enforcement is in progress");
    }
    
    public MachinesSlaEnforcementInProgressException(String message) {
        super("Machines SLA Enforcement is in progress: " + message);
    }
    
    public MachinesSlaEnforcementInProgressException(String message, Exception cause) {
        super("Machines SLA Enforcement is in progress: " + message, cause);
    }
}
