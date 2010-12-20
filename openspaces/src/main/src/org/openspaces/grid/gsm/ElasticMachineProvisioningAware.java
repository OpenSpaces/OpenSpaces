package org.openspaces.grid.gsm;

import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;

public interface ElasticMachineProvisioningAware {

    void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning elasticMachineProvisioning);
}
