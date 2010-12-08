package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;

/*
* The advantage of a state-less topology is that it does not have any inherit 
* scale limits. The disadvantage is that space data , notifications and tasks are
* serialized and sent over the network.
*/
public interface ElasticStatelessDeploymentTopology extends ElasticDeploymentTopology {

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleBeanConfig
     * @see EagerScaleBeanConfigurer
     */
    ElasticStatelessDeploymentTopology enableScaleStrategy(EagerScaleBeanConfigurer configurer);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies. 
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleBeanConfig
     * @see ManualContainersScaleBeanConfigurer
     */
    ElasticStatelessDeploymentTopology enableScaleStrategy(
            ManualContainersScaleBeanConfigurer configurer);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies. 
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleBeanConfig
     * @see ManualContainersScaleBeanConfigurer
     */
    ElasticStatelessDeploymentTopology enableScaleStrategy(
            ManualContainersScaleBeanConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleBeanConfig
     * @see EagerScaleBeanConfigurer
     */
    ElasticStatelessDeploymentTopology enableScaleStrategy(EagerScaleBeanConfig strategy);

}