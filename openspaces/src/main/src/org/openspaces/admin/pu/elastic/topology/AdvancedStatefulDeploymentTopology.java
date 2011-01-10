package org.openspaces.admin.pu.elastic.topology;



public interface AdvancedStatefulDeploymentTopology extends ElasticStatefulDeploymentTopology {

    /**
     * Overrides the number of backup processing unit instances per partition.
     */
    public AdvancedStatefulDeploymentTopology numberOfBackupsPerPartition(int numberOfBackupsPerPartition);
    
    /**
     * Overrides the number of processing unit partitions.
     */
    public AdvancedStatefulDeploymentTopology numberOfPartitions(int numberOfPartitions);

    /**
     * Overrides the maximum number of processing unit instances from the same partition allowed on the same machine.
     * The value 0 disables the limitation, which allows deploying a processing unit on a single machine.
     */
    public AdvancedStatefulDeploymentTopology maxProcessingUnitInstancesFromSamePartitionPerMachine(int maxProcessingUnitInstancesFromSamePartitionPerMachine);

    /**
     * Overrides the minimum number of CPU cores per machine assumption.
     */
    public AdvancedStatefulDeploymentTopology minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine);
}
