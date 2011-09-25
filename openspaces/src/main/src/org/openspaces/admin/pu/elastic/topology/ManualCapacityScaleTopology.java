package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;

public interface ManualCapacityScaleTopology {


    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Manual scale, starts containers until the specified capacity is reached. 
     * Scale strategies can also be reconfigured after deployment.
     * @see ManualCapacityScaleConfig
     * @see ManualCapacityScaleConfigurer
     */
    ManualCapacityScaleTopology scale(
            ManualCapacityScaleConfig strategy);
    
}
