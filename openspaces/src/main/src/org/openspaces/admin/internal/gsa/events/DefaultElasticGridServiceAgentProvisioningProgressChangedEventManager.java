package org.openspaces.admin.internal.gsa.events;

import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEventManager;

public class DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager 
        extends AbstractElasticProcessingUnitProgressChangedEventManager<ElasticGridServiceAgentProvisioningProgressChangedEvent, ElasticGridServiceAgentProvisioningProgressChangedEventListener>
        implements InternalElasticGridServiceAgentProvisioningProgressChangedEventManager {

        public DefaultElasticGridServiceAgentProvisioningProgressChangedEventManager(InternalAdmin admin) {
            super(admin);
        }

        @Override
        public void elasticGridServiceAgentProvisioningProgressChanged(final ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
            super.pushEventToAllListeners(event);
                
        }

        @Override
        protected void fireEventToListener(ElasticGridServiceAgentProvisioningProgressChangedEvent event, ElasticGridServiceAgentProvisioningProgressChangedEventListener listener) {
            listener.elasticGridServiceAgentProvisioningProgressChanged(event);
        }
}
