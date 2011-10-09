package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;

public class MachinesSlaEnforcementPendingContainerDeallocationException extends MachinesSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;
    
    public MachinesSlaEnforcementPendingContainerDeallocationException(ClusterCapacityRequirements clusterCapacityRequirements) {
        super("Cannot terminate the following machines, since they are still running containers:" + clusterCapacityRequirements.toDetailedString());
    }
}
