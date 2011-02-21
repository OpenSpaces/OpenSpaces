package org.openspaces.grid.gsm;

import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;

public interface ElasticMachineProvisioningAware {

    void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning elasticMachineProvisioning);

    void setElasticMachineIsolation(ElasticMachineIsolationConfig isolationConfig);
}
