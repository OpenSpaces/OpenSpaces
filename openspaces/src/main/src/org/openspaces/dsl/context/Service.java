package org.openspaces.dsl.context;

import java.util.Arrays;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

public class Service {

    private final ProcessingUnit pu;
    private final String name;

    // only used for debugging in IntegratedContainer
    private final int planned;

    Service(final ProcessingUnit pu) {
        this.pu = pu;
        this.name = pu.getName();
        planned = 0;
    }

    Service(final String name, final int planned) {
        this.name = name;
        this.pu = null;
        this.planned = planned;

    }

    public void invoke(final String commandName) {
        throw new UnsupportedOperationException("Invoke not implemented yet!");
    }

    public String getName() {
        return name;
    }

    public int getNumberOfPlannedInstances() {
        if (this.pu != null) {
            return pu.getNumberOfInstances();
        } else {
            return planned;
        }
    }

    public int getNumberOfActualInstances() {
        return getInstances().length;
    }

    public ServiceInstance[] getInstances() {

        if (this.pu != null) {
            final ProcessingUnitInstance[] puis = pu.getInstances();
            final ServiceInstance[] sis = new ServiceInstance[puis.length];
            for (int i = 0; i < sis.length; i++) {
                final ProcessingUnitInstance pui = puis[i];
                final ServiceInstance serviceInstance = new ServiceInstance(pui);
                sis[i] = serviceInstance;
            }

            return sis;
        } else {
            return new ServiceInstance[] {new ServiceInstance(null)};
        }

    }

    @Override
    public String toString() {
        return "Service [getName()=" + getName() + ", getNumberOfPlannedInstances()=" + getNumberOfPlannedInstances()
                + ", getNumberOfActualInstances()=" + getNumberOfActualInstances() + ", getInstances()="
                + Arrays.toString(getInstances()) + "]";
    }
    
    

}
