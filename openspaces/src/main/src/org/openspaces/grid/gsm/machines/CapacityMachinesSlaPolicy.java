package org.openspaces.grid.gsm.machines;

import org.openspaces.grid.gsm.capacity.CapacityRequirements;



public class CapacityMachinesSlaPolicy extends AbstractMachinesSlaPolicy {
 
    private CapacityRequirements capacityRequirements;
    
    @Override
    public boolean equals(Object other) {
        return other instanceof CapacityMachinesSlaPolicy &&
        super.equals(other) &&
        ((CapacityMachinesSlaPolicy)other).capacityRequirements.equals(capacityRequirements);
    }

    public CapacityRequirements getCapacityRequirements() {
        return capacityRequirements;
    }
    
    public void setCapacityRequirements(CapacityRequirements capacityRequirements) {
        this.capacityRequirements=capacityRequirements;
    }
    
    public boolean isStopMachineSupported() {
        return true;
    }

    @Override
    public String getScaleStrategyName() {
        return "Manual Capacity Scale Strategy";
    }
}
