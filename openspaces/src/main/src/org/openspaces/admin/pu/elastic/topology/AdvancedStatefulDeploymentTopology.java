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
     * Allows deploying the processing unit on a single machine, by lifting the limitation
     * for primary and backup processing unit instances from the same partition to be deployed on different machines. 
     */
    public AdvancedStatefulDeploymentTopology allowDeploymentOnSingleMachine();

    /**
     * Overrides the minimum number of CPU cores per machine assumption.
     */
    public AdvancedStatefulDeploymentTopology minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine);
}
