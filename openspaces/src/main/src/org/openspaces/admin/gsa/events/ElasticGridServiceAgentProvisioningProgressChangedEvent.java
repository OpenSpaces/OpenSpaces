package org.openspaces.admin.gsa.events;

import org.openspaces.admin.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;

public class ElasticGridServiceAgentProvisioningProgressChangedEvent extends AbstractElasticProcessingUnitProgressChangedEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public ElasticGridServiceAgentProvisioningProgressChangedEvent() {
    }

    public ElasticGridServiceAgentProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }

}
