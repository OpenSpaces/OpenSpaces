package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;

public class DefaultElasticMachineProvisioningProgressChangedEvent 
    extends AbstractElasticProcessingUnitProgressChangedEvent 
    implements ElasticMachineProvisioningProgressChangedEvent{
    
    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public DefaultElasticMachineProvisioningProgressChangedEvent() {
    }
    
    public DefaultElasticMachineProvisioningProgressChangedEvent(boolean isComplete, boolean isUndeploying, String processingUnitName) {
        super(isComplete, isUndeploying, processingUnitName);
    }
}
