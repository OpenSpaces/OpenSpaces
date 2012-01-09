package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FailedToDiscoverMachinesException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public FailedToDiscoverMachinesException(ProcessingUnit pu, Exception cause) {
        super("Machine provisioning failed to discover existing agents. Cause:" + cause.getMessage(), cause);
        this.affectedProcessingUnits = new String[] {pu.getName()};
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof FailedToDiscoverMachinesException) {
            FailedToDiscoverMachinesException otherEx = (FailedToDiscoverMachinesException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits,this.affectedProcessingUnits) && 
                   otherEx.getCause().getMessage().equals(getCause().getMessage());
        }
        return same;  
    }
}
