package org.openspaces.admin.pu.elastic.topology;





public interface AdvancedStatefulDeploymentTopology extends ElasticStatefulDeploymentTopology {

    /**
     * Overrides the number of backup processing unit instances per partition.
     * 
     * Default is 1
     * 
     * This is an advanced property.
     * 
     * @since 8.0 
     */
    public AdvancedStatefulDeploymentTopology numberOfBackupsPerPartition(int numberOfBackupsPerPartition);
    
    /**
     * Defines the number of processing unit partitions.
     * 
     * This property cannot be used with {@link #maxMemoryCapacity(String)} and {@link #maxNumberOfCpuCores(int)}.
     * 
     * This is an advanced property.
     * 
     * @since 8.0
     */
    public AdvancedStatefulDeploymentTopology numberOfPartitions(int numberOfPartitions);

    /**
     * Allows deployment of the processing unit on a single machine, by lifting the limitation
     * for primary and backup processing unit instances from the same partition to be deployed on different machines.
     * Default value is false (by default primary instances and backup instances need separate machines).
     * 
     * This is an advanced property.
     * 
     * @since 8.0
     */
    public AdvancedStatefulDeploymentTopology singleMachineDeployment();

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
    public AdvancedStatefulDeploymentTopology minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine);
    
}
