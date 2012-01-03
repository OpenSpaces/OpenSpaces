package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;

import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FailedToStartNewMachineException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public FailedToStartNewMachineException(String[] affectedProcessingUnits, Exception cause) {
        super("Machine provisioning failed to start a new machine.",cause);
        this.affectedProcessingUnits = affectedProcessingUnits;
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof FailedToStartNewMachineException) {
            FailedToStartNewMachineException otherEx = (FailedToStartNewMachineException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits,this.affectedProcessingUnits) && 
                   otherEx.getCause().getMessage().equals(getCause().getMessage());
        }
        return same;  
    }
}
