package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEvent;

public class DefaultElasticGridServiceAgentProvisioningFailureEvent 
    extends AbstractElasticProcessingUnitFailureEvent 
    implements ElasticGridServiceAgentProvisioningFailureEvent {

private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization constructor
     */
    public DefaultElasticGridServiceAgentProvisioningFailureEvent() {
        super();
    }
    
    public DefaultElasticGridServiceAgentProvisioningFailureEvent(String failureDescription, String[] processingUnitNames) {
        super(failureDescription, processingUnitNames);
    }

}
