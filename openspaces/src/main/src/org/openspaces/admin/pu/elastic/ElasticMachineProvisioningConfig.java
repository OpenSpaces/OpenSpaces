package org.openspaces.admin.pu.elastic;

import org.openspaces.admin.bean.BeanConfig;

public interface ElasticMachineProvisioningConfig extends BeanConfig {

    int getMinimumNumberOfCpuCoresPerMachine();

}
