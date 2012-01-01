package org.openspaces.grid.gsm.machines.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.pu.ProcessingUnit;

public class SomeProcessingUnitsHaveNotCompletedStateRecoveryException extends
        GridServiceAgentSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public SomeProcessingUnitsHaveNotCompletedStateRecoveryException(List<ProcessingUnit> pusNotCompletedStateRecovery) {
        super("Waiting for the following processing units to complete state recovery: " + 
                  pusToString(pusNotCompletedStateRecovery));
    }

    private static String pusToString(List<ProcessingUnit> pus) {
        List<String> puNames = new ArrayList<String>(pus.size());
        for (ProcessingUnit pu : pus) {
            puNames.add(pu.getName());
        }
        return puNames.toString();
    }
}
