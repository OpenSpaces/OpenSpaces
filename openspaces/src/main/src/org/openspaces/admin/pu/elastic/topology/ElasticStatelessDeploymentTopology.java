package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;

/*
* The advantage of a state-less topology is that it does not have any inherit 
* scale limits. The disadvantage is that space data , notifications and tasks are
* serialized and sent over the network.
*/
public interface ElasticStatelessDeploymentTopology extends ElasticDeploymentTopology {


    /**
     * Enables the specified scale strategy, and disables all other scale strategies. 
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualCapacityScaleConfig
     * @see ManualCapacityScaleConfigurer
     */
    ElasticStatelessDeploymentTopology scale(
            ManualCapacityScaleConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleConfig
     * @see EagerScaleConfigurer
     */
    ElasticStatelessDeploymentTopology scale(EagerScaleConfig strategy);

}