package org.openspaces.grid.gsm.machines.backup;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;

public class MachinesStateRecoveryInProgressException extends GridServiceAgentSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public MachinesStateRecoveryInProgressException(ProcessingUnit pu) {
        super(pu, "Machines state is being read from the Space.");
    }
}
