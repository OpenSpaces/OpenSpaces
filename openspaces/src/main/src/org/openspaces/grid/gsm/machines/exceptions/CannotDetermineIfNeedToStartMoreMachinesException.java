package org.openspaces.grid.gsm.machines.exceptions;

public class CannotDetermineIfNeedToStartMoreMachinesException extends GridServiceAgentSlaEnforcementInProgressException {
    
    private static final long serialVersionUID = 1L;

    public CannotDetermineIfNeedToStartMoreMachinesException(int machineShortage) {
        super("Cannot determine if more machines are needed in order to reach the minimum number of machines. Currently short of " + machineShortage + " machines. Will check again later since there are still some machines being started.");
    }
}
