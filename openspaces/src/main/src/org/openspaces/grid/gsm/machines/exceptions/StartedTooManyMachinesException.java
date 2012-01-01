package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;

public class StartedTooManyMachinesException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String[] affectedProcessingUnits;

    public StartedTooManyMachinesException(ProcessingUnit pu, Collection<GridServiceAgent> agents) {
        super("Started too many machines " + MachinesSlaUtils.machinesToString(agents)+". They are not needed by " + pu.getName());
        this.affectedProcessingUnits = new String[] { pu.getName()};
    }
    
    @Override
    public String[] getAffectedProcessingUnits() {
        return affectedProcessingUnits;
    }
            
}
