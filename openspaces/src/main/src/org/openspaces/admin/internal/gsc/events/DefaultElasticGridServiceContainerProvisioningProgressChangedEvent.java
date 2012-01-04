package org.openspaces.admin.internal.gsc.events;

import org.openspaces.admin.gsc.events.ElasticGridServiceContainerProvisioningProgressChangedEvent;
import org.openspaces.admin.internal.pu.elastic.events.AbstractElasticProcessingUnitProgressChangedEvent;

public class DefaultElasticGridServiceContainerProvisioningProgressChangedEvent 
    extends AbstractElasticProcessingUnitProgressChangedEvent 
    implements ElasticGridServiceContainerProvisioningProgressChangedEvent {

    private static final long serialVersionUID = 1L;
    
    /**
     * de-serialization/reflection constructor
     */
    public DefaultElasticGridServiceContainerProvisioningProgressChangedEvent() {
        super();
    }

}
