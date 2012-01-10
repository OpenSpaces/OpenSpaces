package org.openspaces.grid.gsm.machines;

import org.openspaces.admin.Admin;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;

public class UndeployMachinesSlaPolicy extends CapacityMachinesSlaPolicy {

    public UndeployMachinesSlaPolicy(final Admin admin) {
        super();
        super.setMinimumNumberOfMachines(0);
        super.setCapacityRequirements(new CapacityRequirements());
    }
    
    @Override
    public boolean isUndeploying() {
        return true;
    }
    
    @Override
    public String getScaleStrategyName() {
        return "Undeploy Capacity Scale Strategy";
    }
}
