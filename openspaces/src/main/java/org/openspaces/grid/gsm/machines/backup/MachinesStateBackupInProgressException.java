package org.openspaces.grid.gsm.machines.backup;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class MachinesStateBackupInProgressException extends SlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public MachinesStateBackupInProgressException(ProcessingUnit pu) {
        super(pu, "Machines state is being stored in the Space.");
    }
}
