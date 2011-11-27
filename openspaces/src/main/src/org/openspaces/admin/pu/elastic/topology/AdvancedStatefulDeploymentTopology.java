package org.openspaces.admin.pu.elastic.topology;





public interface AdvancedStatefulDeploymentTopology {

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
   
}
