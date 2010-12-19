package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;

/**
 * Represents a configuration object that specifies and configures the {@link ElasticMachineProvisioning} bean
 * that forwards start/stop machine requests to a cloud provider, or a predefined machine-pool. 
 * 
 * @author itaif
 * 
 * @see ElasticMachineProvisioning
 * @see Bean
 */
public interface ElasticMachineProvisioningConfig extends BeanConfig {

}
