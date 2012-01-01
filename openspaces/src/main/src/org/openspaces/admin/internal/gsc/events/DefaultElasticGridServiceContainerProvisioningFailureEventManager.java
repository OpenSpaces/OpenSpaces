package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningFailureEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsa.events.InternalElasticGridServiceContainerProvisioningFailureEventManager;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitFailureEventManager;


public class DefaultElasticGridServiceContainerProvisioningFailureEventManager 
    extends AbstractElasticProcessingUnitFailureEventManager<ElasticGridServiceContainerProvisioningFailureEvent, ElasticGridServiceContainerProvisioningFailureEventListener>    
    implements InternalElasticGridServiceContainerProvisioningFailureEventManager {

    public DefaultElasticGridServiceContainerProvisioningFailureEventManager(InternalAdmin admin) {
        super(admin);
    }

    @Override
    public void elasticGridServiceContainerProvisioningFailure(ElasticGridServiceContainerProvisioningFailureEvent event) {
        super.pushEventToAllListeners(event);
        
    }

    @Override
    protected void fireEventToListener(
            ElasticGridServiceContainerProvisioningFailureEvent event,
            ElasticGridServiceContainerProvisioningFailureEventListener listener) {
        
        listener.elasticGridServiceContainerProvisioningFailure(event);
    }
}
