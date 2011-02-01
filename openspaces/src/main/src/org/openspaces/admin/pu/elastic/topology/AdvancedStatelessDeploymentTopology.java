package org.openspaces.admin.pu.elastic.topology;

public interface AdvancedStatelessDeploymentTopology {

    /**
     * Overrides the minimum number of CPU cores per machine assumption.
     * 
     * By default if {@link ElasticDeploymentTopology#machineProvisioning(org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig)}
     * is set, then the configuration is queried for the minimum number of CPU cores per machine. 
     * By default if the deployment is based on running agents (machineProvisioning is not set), 
     * the agents are queried for the minimum number of CPU cores per machine..
     * 
     * This is an advanced property.
     * 
     * @since 8.0
     */
    public AdvancedStatelessDeploymentTopology minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine);
 
}
