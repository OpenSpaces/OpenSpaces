package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.ManualMemoryCapacityScaleBeanConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfig;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleBeanConfigurer;
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
     * @see EagerScaleBeanConfig
     * @see EagerScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(EagerScaleBeanConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleBeanConfig
     * @see ManualContainersScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualContainersScaleBeanConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualMemoryCapacityScaleBeanConfig
     * @see ManualMemoryCapacityScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualMemoryCapacityScaleBeanConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see MemoryCapacityScaleBeanConfig
     * @see MemoryCapacityScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            MemoryCapacityScaleBeanConfigurer strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleBeanConfig
     * @see EagerScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(EagerScaleBeanConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualContainersScaleBeanConfig
     * @see ManualContainersScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualContainersScaleBeanConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualMemoryCapacityScaleBeanConfig
     * @see ManualMemoryCapacityScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            ManualMemoryCapacityScaleBeanConfig strategy);

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Scale strategies can also be reconfigured after deployment.
     * @see MemoryCapacityScaleBeanConfig
     * @see MemoryCapacityScaleBeanConfigurer
     */
    ElasticStatefulDeploymentTopology scale(
            MemoryCapacityScaleBeanConfig strategy);

}