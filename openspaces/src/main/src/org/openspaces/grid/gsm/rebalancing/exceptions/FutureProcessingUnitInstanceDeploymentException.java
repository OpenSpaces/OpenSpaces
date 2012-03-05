package org.openspaces.grid.gsm.rebalancing.exceptions;

import java.util.Arrays;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class FutureProcessingUnitInstanceDeploymentException extends RebalancingSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;
    
    public FutureProcessingUnitInstanceDeploymentException(ProcessingUnit pu, Throwable cause) {
        super(pu, cause);
        this.affectedProcessingUnits = new String[] { pu.getName() };
    }

    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof FutureProcessingUnitInstanceDeploymentException) {
            FutureProcessingUnitInstanceDeploymentException otherEx = (FutureProcessingUnitInstanceDeploymentException)other;
            same = Arrays.equals(otherEx.affectedProcessingUnits,this.affectedProcessingUnits) && 
                   otherEx.getCause().getMessage() != null &&
                   getCause().getMessage() != null &&
                   otherEx.getCause().getMessage().equals(getCause().getMessage());
        }
        return same;  
    }
}
