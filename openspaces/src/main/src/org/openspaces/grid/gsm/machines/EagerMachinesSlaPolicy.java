package org.openspaces.grid.gsm.machines;


public class EagerMachinesSlaPolicy extends AbstractMachinesSlaPolicy {

    private int maxNumberOfMachines;

    @Override
    public boolean equals(Object other) {
        return other instanceof EagerMachinesSlaPolicy &&
        ((EagerMachinesSlaPolicy)other).maxNumberOfMachines == maxNumberOfMachines &&
        super.equals(other);
    }

    public void setMaximumNumberOfMachines(int maxNumberOfMachines) {
        this.maxNumberOfMachines = maxNumberOfMachines;
    }
    
    public int getMaximumNumberOfMachines() {
        return this.maxNumberOfMachines;
    }

}
