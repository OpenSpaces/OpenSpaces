package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;

public class DefaultElasticGridServiceContainerProvisioningFailureEvent 
    extends AbstractElasticProcessingUnitFailureEvent 
    implements ElasticGridServiceContainerProvisioningFailureEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public DefaultElasticGridServiceContainerProvisioningFailureEvent() {
        super();
    }
    
    public DefaultElasticGridServiceContainerProvisioningFailureEvent(String failureDescription, String[] processingUnitNames) {
        super(failureDescription,processingUnitNames);
    }
}
