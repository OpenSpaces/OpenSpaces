package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfigurer;
import org.openspaces.core.util.MemoryUnit;

public interface ElasticStatefulDeploymentTopology extends ElasticDeploymentTopology {

    /**
     * Specifies an estimate of the maximum memory capacity for this processing unit.
     * The actual maximum memory capacity will be at least the specified maximum.
     * Requires the vmInputArgument("-Xmx...") property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit);

    /**
     * Specifies an estimate of the minimum memory capacity for this processing unit.
     * The actual maximum memory capacity will be at least the specified maximum.
     * Requires the vmInputArgument("-Xmx...") property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology maxMemoryCapacity(String maxMemoryCapacity);

    /**
     * Specifies an estimate of the minimum memory capacity for this processing unit.
     * The actual minimum memory capacity will be at least the specified minimum.
     * Requires the vmInputArgument("-Xmx...") property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology minMemoryCapacity(int minMemoryCapacity, MemoryUnit unit);

    /**
     * Specifies an estimate of the minimum memory capacity for this processing unit.
     * The actual minimum memory capacity will be at least the specified minimum.
     * Requires the vmInputArgument("-Xmx...") property.
     * The memory capacity value is the sum of both the primary and backup instances memory capacity.
     */
    ElasticStatefulDeploymentTopology minMemoryCapacity(String minMemoryCapacity);

    /**
     * Specifies if the space should duplicate each information on two different machines.
     * If set to false then partition data is lost each time fail-over or scaling occurs.
     * By default highlyAvailable is true
     */
    ElasticStatefulDeploymentTopology highlyAvailable(boolean highlyAvailable);
    
    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleConfig
     * @see EagerScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(EagerScaleConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleConfig
     * @see ManualContainersScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualContainersScaleConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualCapacityScaleConfig
     * @see ManualCapacityScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualCapacityScaleConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see MemoryCapacityScaleConfig
     * @see MemoryCapacityScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            MemoryCapacityScaleConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleConfig
     * @see EagerScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(EagerScaleConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleConfig
     * @see ManualContainersScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualContainersScaleConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualCapacityScaleConfig
     * @see ManualCapacityScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualCapacityScaleConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see MemoryCapacityScaleConfig
     * @see MemoryCapacityScaleConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            MemoryCapacityScaleConfig strategy);
}