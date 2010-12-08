package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.ElasticReplicatedProcessingUnitDeployment;

/**
 * Defines an elastic processing unit deployment that contains an embedded replicated space.
 * 
 * The advantage of a replicated (active/active topology) is that it allows multiple replicas 
 * of the same data. The disadvantage is that the size of the data is limited by the memory available
 * of a single machine.
 * 
 * @author itaif
 */
public interface ElasticReplicatedDeploymentTopology extends ElasticDeploymentTopology {

    /**
     * Specifies a hard limit to the maximum number of containers for this processing unit.
     * If not specified the default {@link ElasticReplicatedProcessingUnitDeployment#MAX_NUMBER_OF_CONTAINERS_DEFAULT} applies.
     */
    ElasticReplicatedDeploymentTopology numberOfContainers(int maxNumberOfContainers);
    
}
