package org.openspaces.grid.gsm.machines.exceptions;


public class WaitingForDiscoveredMachinesException extends MachinesSlaEnforcementInProgressException {
    
    private static final long serialVersionUID = 1L;
        
    public WaitingForDiscoveredMachinesException(String message) {
        super(message);
    }   
}