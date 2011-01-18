package org.openspaces.grid.gsm.machines;


public class EagerMachinesSlaPolicy extends AbstractMachinesSlaPolicy {

    @Override
    public boolean equals(Object other) {
        return other instanceof EagerMachinesSlaPolicy &&
        super.equals(other);
    }

}
