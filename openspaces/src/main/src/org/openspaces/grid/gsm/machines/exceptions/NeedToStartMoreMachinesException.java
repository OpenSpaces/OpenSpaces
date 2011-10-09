package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementException;

public class NeedToStartMoreMachinesException extends SlaEnforcementException {
    
    private static final long serialVersionUID = 1L;

    public NeedToStartMoreMachinesException(CapacityRequirements capacityShortage) {
        super("Cannot enforce Machines SLA since there are not enough machines available. "+
              "Need more machines with " + capacityShortage);        
    }
}