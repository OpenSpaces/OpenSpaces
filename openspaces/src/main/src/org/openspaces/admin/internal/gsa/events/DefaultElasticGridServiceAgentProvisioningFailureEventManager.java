package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningFailureEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEventManager;


public class DefaultElasticGridServiceAgentProvisioningFailureEventManager 
extends AbstractElasticProcessingUnitFailureEventManager<ElasticGridServiceAgentProvisioningFailureEvent, ElasticGridServiceAgentProvisioningFailureEventListener>    
implements InternalElasticGridServiceAgentProvisioningFailureEventManager {

    public DefaultElasticGridServiceAgentProvisioningFailureEventManager(InternalAdmin admin) {
        super(admin);
    }

    @Override
    public void elasticGridServiceAgentProvisioningFailure(ElasticGridServiceAgentProvisioningFailureEvent event) {
        super.pushEventToAllListeners(event);
        
    }

    @Override
    protected void fireEventToListener(
            ElasticGridServiceAgentProvisioningFailureEvent event,
            ElasticGridServiceAgentProvisioningFailureEventListener listener) {
        
        listener.elasticGridServiceAgentProvisioningFailure(event);
    }
}
