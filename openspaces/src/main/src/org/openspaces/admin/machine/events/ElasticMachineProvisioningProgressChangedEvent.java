package org.openspaces.admin.machine.events;

import org.openspaces.admin.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;

public class ElasticMachineProvisioningProgressChangedEvent extends AbstractElasticProcessingUnitProgressChangedEvent {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public ElasticMachineProvisioningProgressChangedEvent() {
    }
    
    public ElasticMachineProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }
}
