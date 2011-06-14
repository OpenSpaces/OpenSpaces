package org.openspaces.dsl.context;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

public class Service {

    private final ProcessingUnit pu;

    Service(final ProcessingUnit pu) {
        this.pu = pu;
    }

    public void invoke(final String commandName) {
        throw new UnsupportedOperationException("Invoke not implemented yet!");
    }

    public String getName() {
        return pu.getName();
    }

    public int getNumberOfPlannedInstances() {
        return pu.getNumberOfInstances();
    }

    public int getNumberOfActualInstances() {
        return getInstances().length;
    }

    public ServiceInstance[] getInstances() {

        final ProcessingUnitInstance[] puis = pu.getInstances();
        final ServiceInstance[] sis = new ServiceInstance[puis.length];
        for (int i = 0; i < sis.length; i++) {
            final ProcessingUnitInstance pui = puis[i];
            final ServiceInstance serviceInstance = new ServiceInstance(pui);
            sis[i] = serviceInstance;
        }

        return sis;

    }

}
