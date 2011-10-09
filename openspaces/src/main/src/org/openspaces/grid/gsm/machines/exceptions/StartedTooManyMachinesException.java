package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;

public class StartedTooManyMachinesException extends MachinesSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public StartedTooManyMachinesException(ProcessingUnit pu, Collection<GridServiceAgent> agents) {
        super("Started too many machines " + MachinesSlaUtils.machinesToString(agents)+". They are not needed by " + pu.getName());
    }
            
}
