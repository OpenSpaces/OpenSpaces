package org.openspaces.admin.internal.gsc;

import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainers extends GridServiceContainers {

    void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer);

    InternalGridServiceContainer removeGridServiceContainer(String uid);

    /**
     * If relevant raises events to relevant subscribers
     * @since 8.0.6
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);
}