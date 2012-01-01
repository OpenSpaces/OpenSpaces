package org.openspaces.admin.gsc.events;

import org.openspaces.admin.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;

public class ElasticGridServiceContainerProvisioningProgressChangedEvent extends AbstractElasticProcessingUnitProgressChangedEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public ElasticGridServiceContainerProvisioningProgressChangedEvent() {
    }

    
    public ElasticGridServiceContainerProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }
}
