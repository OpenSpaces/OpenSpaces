package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;

public interface EagerScaleTopology {

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Eager scale starts new containers on any available agent.
     * Scale strategies can also be reconfigured after deployment.
     * @see EagerScaleConfig
     * @see EagerScaleConfigurer
     */
    EagerScaleTopology scale(EagerScaleConfig strategy);
}
