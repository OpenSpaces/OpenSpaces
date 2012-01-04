package org.openspaces.admin.internal.pu.elastic.events;

import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitInstanceProvisioningFailureEvent;

public class DefaultElasticProcessingUnitInstanceProvisioningFailureEvent 
        extends AbstractElasticProcessingUnitFailureEvent
        implements ElasticProcessingUnitInstanceProvisioningFailureEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization/reflection constructor
     */
    public DefaultElasticProcessingUnitInstanceProvisioningFailureEvent() {
        super();
    }
}
