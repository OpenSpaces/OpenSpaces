package org.openspaces.grid.gsm.machines.exceptions;

import java.util.Arrays;
import java.util.Collection;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;

public class InconsistentMachineProvisioningException extends MachinesSlaEnforcementInProgressException{
    
    private static final long serialVersionUID = 1L;

    public InconsistentMachineProvisioningException(Collection<GridServiceAgent> undiscoveredAgents) {
        super("Machines " + MachinesSlaUtils.machinesToString(undiscoveredAgents)+ " have not been discoved yet by the machine provisioning.");
    }
    
    public InconsistentMachineProvisioningException(GridServiceAgent undiscoveredAgents) {
        this(Arrays.asList(new GridServiceAgent[] {undiscoveredAgents}));
    }
    
}
