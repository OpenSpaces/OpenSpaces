package org.openspaces.admin.internal.gsa;

import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

/**
 * @author kimchy
 */
public interface InternalGridServiceAgents extends GridServiceAgents {

    void addGridServiceAgent(InternalGridServiceAgent gridServiceAgent);

    InternalGridServiceAgent removeGridServiceAgent(String uid);
    
    /**
     * If relevant raises events to relevant subscribers
     * @since 8.0.6
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);
}