package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEventManager;


public class DefaultElasticGridServiceContainerProvisioningProgressChangedEventManager 
        extends AbstractElasticProcessingUnitProgressChangedEventManager<ElasticGridServiceContainerProvisioningProgressChangedEvent, ElasticGridServiceContainerProvisioningProgressChangedEventListener>
        implements InternalElasticGridServiceContainerProvisioningProgressChangedEventManager {

        public DefaultElasticGridServiceContainerProvisioningProgressChangedEventManager(InternalAdmin admin) {
            super(admin);
        }

        @Override
        public void elasticGridServiceContainerProvisioningProgressChanged(final ElasticGridServiceContainerProvisioningProgressChangedEvent event) {
            super.pushEventToAllListeners(event);
                
        }
        
        @Override
        protected void fireEventToListener(ElasticGridServiceContainerProvisioningProgressChangedEvent event, ElasticGridServiceContainerProvisioningProgressChangedEventListener listener) {
            listener.elasticGridServiceContainerProvisioningProgressChanged(event);
        }

}
